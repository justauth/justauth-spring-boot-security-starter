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

import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.lang.Nullable;

import java.time.Duration;

/**
 * {@link IRedisHashCacheWriter} provides low level access to Redis commands ({@code HSET, HMSET, HSETNX, HGET, HMGET
 * HGETALL, HEXPIRE,...}) used for caching. <br />
 * The {@link IRedisHashCacheWriter} may be shared by multiple cache implementations and is responsible for writing / reading
 * binary data to / from Redis. The implementation honors potential cache lock flags that might be set.
 * @see RedisCacheWriter
 * @author YongWu zheng
 * @version V1.0  Created by 2020/6/13 14:15
 */
public interface IRedisHashCacheWriter extends RedisCacheWriter {

	/**
	 * Write the given (key, field)/value pair to Redis an set the expiration time if defined.
	 *
	 * @param name The cache name must not be {@literal null}.
	 * @param key The key for the cache entry. Must not be {@literal null}.
	 * @param field The field for the cache entry. Must not be {@literal null}.
	 * @param value The value stored for the key. Must not be {@literal null}.
	 * @param ttl Optional expiration time. Can be {@literal null}.
	 */
	void hPut(String name, byte[] key, byte[] field, byte[] value, @Nullable Duration ttl);

	/**
	 * Get the binary value representation from Redis stored for the given key and field.
	 *
	 * @param name must not be {@literal null}.
	 * @param key must not be {@literal null}.
	 * @param field The field for the cache entry. Must not be {@literal null}.
	 * @return {@literal null} if key does not exist.
	 */
	@Nullable
	byte[] hGet(String name, byte[] key, byte[] field);

	/**
	 * Write the given value to Redis if the key does not already exist.
	 *
	 * @param name The cache name must not be {@literal null}.
	 * @param key The key for the cache entry. Must not be {@literal null}.
	 * @param field The field for the cache entry. Must not be {@literal null}.
	 * @param value The value stored for the key. Must not be {@literal null}.
	 * @param ttl Optional expiration time. Can be {@literal null}.
	 * @return {@literal null} if the value has been written, the value stored for the key if it already exists.
	 */
	@Nullable
	byte[] hPutIfAbsent(String name, byte[] key, byte[] field, byte[] value, @Nullable Duration ttl);

	/**
	 * Remove the given key and field from Redis.
	 *
	 * @param name The cache name must not be {@literal null}.
	 * @param key The key for the cache entry. Must not be {@literal null}.
	 * @param field The field for the cache entry. Must not be {@literal null}.
	 */
	void hRemove(String name, byte[] key, byte[] field);

}