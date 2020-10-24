/*
 * MIT License
 * Copyright (c) 2020-2029 YongWu zheng (dcenter.top and gitee.com/pcore and github.com/ZeroOrInfinity)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package top.dcenter.ums.security.core.oauth.repository.jdbc.cache;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

/**
 * 对 RedisCacheWriter 进行了扩展, 添加了对 Hash类型的缓存的支持. 实现了 {@link IRedisHashCacheWriter}
 * @author YongWu zheng
 * @version V1.0  Created by 2020/6/13 14:15
 *
 * {@link RedisCacheWriter} implementation capable of reading/writing binary data from/to Redis in {@literal standalone}
 * and {@literal cluster} environments. Works upon a given {@link RedisConnectionFactory} to obtain the actual
 * {@link RedisConnection}. <br />
 * {@link RedisHashCacheWriter} can be used in
 * {@link RedisCacheWriter#lockingRedisCacheWriter(RedisConnectionFactory) locking} or
 * {@link RedisCacheWriter#nonLockingRedisCacheWriter(RedisConnectionFactory) non-locking} mode. While
 * {@literal non-locking} aims for maximum performance it may result in overlapping, non atomic, command execution for
 * operations spanning multiple Redis interactions like {@code putIfAbsent}. The {@literal locking} counterpart prevents
 * command overlap by setting an explicit lock key and checking against presence of this key which leads to additional
 * requests and potential command wait times.
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 2.0
 */
class RedisHashCacheWriter implements IRedisHashCacheWriter {

	private final RedisConnectionFactory connectionFactory;
	private final Duration sleepTime;

	/**
	 * @param connectionFactory must not be {@literal null}.
	 */
	RedisHashCacheWriter(RedisConnectionFactory connectionFactory) {
		this(connectionFactory, Duration.ZERO);
	}

	/**
	 * @param connectionFactory must not be {@literal null}.
	 * @param sleepTime sleep time between lock request attempts. Must not be {@literal null}. Use {@link Duration#ZERO}
	 *          to disable locking.
	 */
	RedisHashCacheWriter(RedisConnectionFactory connectionFactory, Duration sleepTime) {

		Assert.notNull(connectionFactory, "ConnectionFactory must not be null!");
		Assert.notNull(sleepTime, "SleepTime must not be null!");

		this.connectionFactory = connectionFactory;
		this.sleepTime = sleepTime;
	}

	@Override
	public void put(@NonNull String name, @NonNull byte[] key, @NonNull byte[] value, @Nullable Duration ttl) {

		Assert.notNull(name, "Name must not be null!");
		Assert.notNull(key, "Key must not be null!");
		Assert.notNull(value, "Value must not be null!");

		execute(name, connection -> {

			if (shouldExpireWithin(ttl)) {
				connection.set(key, value, Expiration.from(ttl.toMillis(), TimeUnit.MILLISECONDS), SetOption.upsert());
			} else {
				connection.set(key, value);
			}

			return "OK";
		});
	}

	@Override
	public void hPut(String name, byte[] key, byte[] field, byte[] value, Duration ttl) {
		Assert.notNull(name, "Name must not be null!");
		Assert.notNull(key, "Key must not be null!");
		Assert.notNull(field, "field must not be null!");
		Assert.notNull(value, "Value must not be null!");

		execute(name, connection -> {

			if (shouldExpireWithin(ttl)) {
				connection.multi();
				connection.expire(key, ttl.getSeconds());
				connection.hSet(key, field, value);
				connection.expire(key, ttl.getSeconds());
				connection.exec();
			} else {
				connection.hSet(key, field, value);
			}

			return "OK";
		});

	}

	@Override
	public byte[] get(@NonNull String name, @NonNull byte[] key) {

		Assert.notNull(name, "Name must not be null!");
		Assert.notNull(key, "Key must not be null!");

		return execute(name, connection -> connection.get(key));
	}

	@Override
	public byte[] hGet(String name, byte[] key, byte[] field) {
		Assert.notNull(name, "Name must not be null!");
		Assert.notNull(field, "field must not be null!");
		Assert.notNull(key, "Key must not be null!");

		return execute(name, connection -> connection.hGet(key,field));
	}

	@Override
	public byte[] putIfAbsent(@NonNull String name, @NonNull byte[] key, @NonNull byte[] value, @Nullable Duration ttl) {

		Assert.notNull(name, "Name must not be null!");
		Assert.notNull(key, "Key must not be null!");
		Assert.notNull(value, "Value must not be null!");

		return execute(name, connection -> {

			if (isLockingCacheWriter()) {
				doLock(name, connection);
			}

			try {
				if (ofNullable(connection.setNX(key, value)).orElse(Boolean.FALSE))
				{

					if (shouldExpireWithin(ttl))
					{
						connection.pExpire(key, ttl.toMillis());
					}
					return null;
				}

				return connection.get(key);
			} finally {

				if (isLockingCacheWriter()) {
					doUnlock(name, connection);
				}
			}
		});
	}


	@Override
	public byte[] hPutIfAbsent(String name, byte[] key, byte[] field, byte[] value, Duration ttl) {
		Assert.notNull(name, "Name must not be null!");
		Assert.notNull(key, "Key must not be null!");
		Assert.notNull(field, "field must not be null!");
		Assert.notNull(value, "Value must not be null!");

		return execute(name, connection -> {

			if (isLockingCacheWriter()) {
				doLock(name, connection);
			}

			try {
				if (ofNullable(connection.hSetNX(key, field, value)).orElse(Boolean.FALSE))
				{

					if (shouldExpireWithin(ttl))
					{
						connection.pExpire(key, ttl.toMillis());
					}
					return null;
				}

				return connection.hGet(key, field);
			} finally {

				if (isLockingCacheWriter()) {
					doUnlock(name, connection);
				}
			}
		});
	}

	@Override
	public void remove(@NonNull String name, @NonNull byte[] key) {

		Assert.notNull(name, "Name must not be null!");
		Assert.notNull(key, "Key must not be null!");

		execute(name, connection -> connection.del(key));
	}


	@Override
	public void hRemove(String name, byte[] key, byte[] field) {
		Assert.notNull(name, "Name must not be null!");
		Assert.notNull(field, "field must not be null!");
		Assert.notNull(key, "Key must not be null!");

		execute(name, connection -> connection.hDel(key, field));

	}

	@Override
	public void clean(@NonNull String name, @NonNull byte[] pattern) {

		Assert.notNull(name, "Name must not be null!");
		Assert.notNull(pattern, "Pattern must not be null!");

		execute(name, connection -> {

			boolean wasLocked = false;

			try {

				if (isLockingCacheWriter()) {
					doLock(name, connection);
					wasLocked = true;
				}

				byte[][] keys = ofNullable(connection.keys(pattern)).orElse(Collections.emptySet())
						.toArray(new byte[0][]);

				if (keys.length > 0) {
					connection.del(keys);
				}
			} finally {

				if (wasLocked && isLockingCacheWriter()) {
					doUnlock(name, connection);
				}
			}

			return "OK";
		});
	}

	/**
	 * Explicitly set a write lock on a cache.
	 *
	 * @param name the name of the cache to lock.
	 */
	@SuppressWarnings("unused")
	void lock(String name) {
		execute(name, connection -> doLock(name, connection));
	}

	/**
	 * Explicitly remove a write lock from a cache.
	 *
	 * @param name the name of the cache to unlock.
	 */
	@SuppressWarnings("unused")
	void unlock(String name) {
		executeLockFree(connection -> doUnlock(name, connection));
	}

	private Boolean doLock(String name, RedisConnection connection) {
		return connection.setNX(createCacheLockKey(name), new byte[0]);
	}

	@SuppressWarnings("UnusedReturnValue")
	private Long doUnlock(String name, RedisConnection connection) {
		return connection.del(createCacheLockKey(name));
	}

	boolean doCheckLock(String name, RedisConnection connection) {
		return ofNullable(connection.exists(createCacheLockKey(name))).orElse(Boolean.FALSE);
	}

	/**
	 * @return {@literal true} if {@link RedisCacheWriter} uses locks.
	 */
	private boolean isLockingCacheWriter() {
		return !sleepTime.isZero() && !sleepTime.isNegative();
	}

	private <T> T execute(String name, Function<RedisConnection, T> callback) {

		RedisConnection connection = connectionFactory.getConnection();
		//noinspection TryFinallyCanBeTryWithResources
		try {

			checkAndPotentiallyWaitUntilUnlocked(name, connection);
			return callback.apply(connection);
		} finally {
			connection.close();
		}
	}

	private void executeLockFree(Consumer<RedisConnection> callback) {

		RedisConnection connection = connectionFactory.getConnection();

		//noinspection TryFinallyCanBeTryWithResources
		try {
			callback.accept(connection);
		} finally {
			connection.close();
		}
	}

	private void checkAndPotentiallyWaitUntilUnlocked(String name, RedisConnection connection) {

		if (!isLockingCacheWriter()) {
			return;
		}

		try {

			while (doCheckLock(name, connection)) {
				//noinspection BusyWait
				Thread.sleep(sleepTime.toMillis());
			}
		} catch (InterruptedException ex) {

			// Re-interrupt current thread, to allow other participants to react.
			Thread.currentThread().interrupt();

			throw new PessimisticLockingFailureException(String.format("Interrupted while waiting to unlock cache %s", name),
					ex);
		}
	}

	private static boolean shouldExpireWithin(@Nullable Duration ttl) {
		return ttl != null && !ttl.isZero() && !ttl.isNegative();
	}

	private static byte[] createCacheLockKey(String name) {
		return (name + "~lock").getBytes(StandardCharsets.UTF_8);
	}

}