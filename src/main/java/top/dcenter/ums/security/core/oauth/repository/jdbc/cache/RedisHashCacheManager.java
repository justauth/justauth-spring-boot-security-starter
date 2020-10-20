/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.dcenter.ums.security.core.oauth.repository.jdbc.cache;

import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 对 {@link org.springframework.data.redis.cache.RedisCacheManager} 进行了扩展, 添加了对 Hash类型的缓存的支持
 * @author YongWu zheng
 * @version V1.0  Created by 2020/6/13 14:15
 *
 * {@link org.springframework.cache.CacheManager} backed by a {@link RedisHashCache Redis} cache.
 * <p />
 * This cache manager creates caches by default upon first write. Empty caches are not visible on Redis due to how Redis
 * represents empty data structures.
 * <p />
 * Caches requiring a different {@link RedisCacheConfiguration} than the default configuration can be specified via
 * {@link RedisHashCacheManagerBuilder#withInitialCacheConfigurations(Map)}.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @author YongWu zheng
 * @since 2.0
 * @see RedisCacheConfiguration
 * @see RedisHashCacheWriter
 */
@SuppressWarnings({"AlibabaCommentsMustBeJavadocFormat", "unused"})
public class RedisHashCacheManager extends AbstractTransactionSupportingCacheManager {

	private final RedisHashCacheWriter cacheWriter;
	private final RedisCacheConfiguration defaultCacheConfig;
	private final Map<String, RedisCacheConfiguration> initialCacheConfiguration;
	private final boolean allowInFlightCacheCreation;

	/**
	 * Creates new {@link RedisHashCacheManager} using given {@link RedisHashCacheWriter} and default
	 * {@link RedisCacheConfiguration}.
	 *
	 * @param cacheWriter must not be {@literal null}.
	 * @param defaultCacheConfiguration must not be {@literal null}. Maybe just use
	 *          {@link RedisCacheConfiguration#defaultCacheConfig()}.
	 * @param allowInFlightCacheCreation allow create unconfigured caches.
	 * @since 2.0.4
	 */
	private RedisHashCacheManager(RedisHashCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration,
	                              boolean allowInFlightCacheCreation) {

		Assert.notNull(cacheWriter, "CacheWriter must not be null!");
		Assert.notNull(defaultCacheConfiguration, "DefaultCacheConfiguration must not be null!");

		this.cacheWriter = cacheWriter;
		this.defaultCacheConfig = defaultCacheConfiguration;
		this.initialCacheConfiguration = new LinkedHashMap<>();
		this.allowInFlightCacheCreation = allowInFlightCacheCreation;
	}

	/**
	 * Creates new {@link RedisHashCacheManager} using given {@link RedisHashCacheWriter} and default
	 * {@link RedisCacheConfiguration}.
	 *
	 * @param cacheWriter must not be {@literal null}.
	 * @param defaultCacheConfiguration must not be {@literal null}. Maybe just use
	 *          {@link RedisCacheConfiguration#defaultCacheConfig()}.
	 */
	public RedisHashCacheManager(RedisHashCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
		this(cacheWriter, defaultCacheConfiguration, true);
	}

	/**
	 * Creates new {@link RedisHashCacheManager} using given {@link RedisHashCacheWriter} and default
	 * {@link RedisCacheConfiguration}.
	 *
	 * @param cacheWriter must not be {@literal null}.
	 * @param defaultCacheConfiguration must not be {@literal null}. Maybe just use
	 *          {@link RedisCacheConfiguration#defaultCacheConfig()}.
	 * @param initialCacheNames optional set of known cache names that will be created with given
	 *          {@literal defaultCacheConfiguration}.
	 */
	public RedisHashCacheManager(RedisHashCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration,
	                             String... initialCacheNames) {

		this(cacheWriter, defaultCacheConfiguration, true, initialCacheNames);
	}

	/**
	 * Creates new {@link RedisHashCacheManager} using given {@link RedisHashCacheWriter} and default
	 * {@link RedisCacheConfiguration}.
	 *
	 * @param cacheWriter must not be {@literal null}.
	 * @param defaultCacheConfiguration must not be {@literal null}. Maybe just use
	 *          {@link RedisCacheConfiguration#defaultCacheConfig()}.
	 * @param allowInFlightCacheCreation if set to {@literal true} no new caches can be acquire at runtime but limited to
	 *          the given list of initial cache names.
	 * @param initialCacheNames optional set of known cache names that will be created with given
	 *          {@literal defaultCacheConfiguration}.
	 * @since 2.0.4
	 */
	public RedisHashCacheManager(RedisHashCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration,
	                             boolean allowInFlightCacheCreation, String... initialCacheNames) {

		this(cacheWriter, defaultCacheConfiguration, allowInFlightCacheCreation);

		for (String cacheName : initialCacheNames) {
			this.initialCacheConfiguration.put(cacheName, defaultCacheConfiguration);
		}
	}

	/**
	 * Creates new {@link RedisHashCacheManager} using given {@link RedisHashCacheWriter} and default
	 * {@link RedisCacheConfiguration}.
	 *
	 * @param cacheWriter must not be {@literal null}.
	 * @param defaultCacheConfiguration must not be {@literal null}. Maybe just use
	 *          {@link RedisCacheConfiguration#defaultCacheConfig()}.
	 * @param initialCacheConfigurations Map of known cache names along with the configuration to use for those caches.
	 *          Must not be {@literal null}.
	 */
	public RedisHashCacheManager(RedisHashCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration,
	                             Map<String, RedisCacheConfiguration> initialCacheConfigurations) {

		this(cacheWriter, defaultCacheConfiguration, initialCacheConfigurations, true);
	}

	/**
	 * Creates new {@link RedisHashCacheManager} using given {@link RedisHashCacheWriter} and default
	 * {@link RedisCacheConfiguration}.
	 *
	 * @param cacheWriter must not be {@literal null}.
	 * @param defaultCacheConfiguration must not be {@literal null}. Maybe just use
	 *          {@link RedisCacheConfiguration#defaultCacheConfig()}.
	 * @param initialCacheConfigurations Map of known cache names along with the configuration to use for those caches.
	 *          Must not be {@literal null}.
	 * @param allowInFlightCacheCreation if set to {@literal false} this cache manager is limited to the initial cache
	 *          configurations and will not create new caches at runtime.
	 * @since 2.0.4
	 */
	public RedisHashCacheManager(RedisHashCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration,
	                             Map<String, RedisCacheConfiguration> initialCacheConfigurations, boolean allowInFlightCacheCreation) {

		this(cacheWriter, defaultCacheConfiguration, allowInFlightCacheCreation);

		Assert.notNull(initialCacheConfigurations, "InitialCacheConfigurations must not be null!");

		this.initialCacheConfiguration.putAll(initialCacheConfigurations);
	}

	/**
	 * Create a new {@link RedisHashCacheManager} with defaults applied.
	 * <dl>
	 * <dt>locking</dt>
	 * <dd>disabled</dd>
	 * <dt>cache configuration</dt>
	 * <dd>{@link RedisCacheConfiguration#defaultCacheConfig()}</dd>
	 * <dt>initial caches</dt>
	 * <dd>none</dd>
	 * <dt>transaction aware</dt>
	 * <dd>no</dd>
	 * <dt>in-flight cache creation</dt>
	 * <dd>enabled</dd>
	 * </dl>
	 *
	 * @param connectionFactory must not be {@literal null}.
	 * @return new instance of {@link RedisHashCacheManager}.
	 */
	public static RedisHashCacheManager create(RedisConnectionFactory connectionFactory) {

		Assert.notNull(connectionFactory, "ConnectionFactory must not be null!");

		return new RedisHashCacheManager(new RedisHashCacheWriter(connectionFactory),
		                                 RedisCacheConfiguration.defaultCacheConfig());
	}

	/**
	 * Entry point for builder style {@link RedisHashCacheManager} configuration.
	 *
	 * @return new {@link RedisHashCacheManagerBuilder}.
	 * @since 2.3
	 */
	public static RedisHashCacheManagerBuilder builder() {
		return new RedisHashCacheManagerBuilder();
	}

	/**
	 * Entry point for builder style {@link RedisHashCacheManager} configuration.
	 *
	 * @param connectionFactory must not be {@literal null}.
	 * @return new {@link RedisHashCacheManagerBuilder}.
	 */
	public static RedisHashCacheManagerBuilder builder(RedisConnectionFactory connectionFactory) {

		Assert.notNull(connectionFactory, "ConnectionFactory must not be null!");

		return RedisHashCacheManagerBuilder.fromConnectionFactory(connectionFactory);
	}

	/**
	 * Entry point for builder style {@link RedisHashCacheManager} configuration.
	 *
	 * @param cacheWriter must not be {@literal null}.
	 * @return new {@link RedisHashCacheManagerBuilder}.
	 */
	public static RedisHashCacheManagerBuilder builder(RedisHashCacheWriter cacheWriter) {

		Assert.notNull(cacheWriter, "CacheWriter must not be null!");

		return RedisHashCacheManagerBuilder.fromCacheWriter(cacheWriter);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.cache.support.AbstractCacheManager#loadCaches()
	 */
	@NonNull
	@Override
	protected Collection<RedisCache> loadCaches() {

		List<RedisCache> caches = new LinkedList<>();

		for (Map.Entry<String, RedisCacheConfiguration> entry : initialCacheConfiguration.entrySet()) {
			caches.add(createRedisCache(entry.getKey(), entry.getValue()));
		}

		return caches;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.cache.support.AbstractCacheManager#getMissingCache(java.lang.String)
	 */
	@Override
	protected RedisCache getMissingCache(@NonNull String name) {
		return allowInFlightCacheCreation ? createRedisCache(name, defaultCacheConfig) : null;
	}

	/**
	 * @return unmodifiable {@link Map} containing cache name / configuration pairs. Never {@literal null}.
	 */
	public Map<String, RedisCacheConfiguration> getCacheConfigurations() {

		Map<String, RedisCacheConfiguration> configurationMap = new HashMap<>(getCacheNames().size());

		getCacheNames().forEach(it -> {

			//noinspection RedundantClassCall
			RedisCache cache = RedisCache.class.cast(lookupCache(it));
			configurationMap.put(it, cache != null ? cache.getCacheConfiguration() : null);
		});

		return Collections.unmodifiableMap(configurationMap);
	}

	/**
	 * Configuration hook for creating {@link RedisCache} with given name and {@code cacheConfig}.
	 *
	 * @param name must not be {@literal null}.
	 * @param cacheConfig can be {@literal null}.
	 * @return never {@literal null}.
	 */
	protected RedisCache createRedisCache(String name, @Nullable RedisCacheConfiguration cacheConfig) {
		return new RedisHashCache(name, cacheWriter, cacheConfig != null ? cacheConfig : defaultCacheConfig);
	}

	/**
	 * Configurator for creating {@link RedisHashCacheManager}.
	 *
	 * @author Christoph Strobl
	 * @author Mark Paluch
	 * @author Kezhu Wang
	 * @since 2.0
	 */
	public static class RedisHashCacheManagerBuilder {

		private RedisHashCacheWriter cacheWriter;
		private RedisCacheConfiguration defaultCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
		private final Map<String, RedisCacheConfiguration> initialCaches = new LinkedHashMap<>();
		private boolean enableTransactions;
		boolean allowInFlightCacheCreation = true;

		private RedisHashCacheManagerBuilder() {}

		private RedisHashCacheManagerBuilder(@Nullable RedisHashCacheWriter cacheWriter) {
			this.cacheWriter = cacheWriter;
		}

		/**
		 * Entry point for builder style {@link RedisHashCacheManager} configuration.
		 *
		 * @param connectionFactory must not be {@literal null}.
		 * @return new {@link RedisHashCacheManagerBuilder}.
		 */
		public static RedisHashCacheManagerBuilder fromConnectionFactory(RedisConnectionFactory connectionFactory) {

			Assert.notNull(connectionFactory, "ConnectionFactory must not be null!");

			return new RedisHashCacheManagerBuilder(new RedisHashCacheWriter(connectionFactory));
		}

		/**
		 * Entry point for builder style {@link RedisHashCacheManager} configuration.
		 *
		 * @param cacheWriter must not be {@literal null}.
		 * @return new {@link RedisHashCacheManagerBuilder}.
		 */
		public static RedisHashCacheManagerBuilder fromCacheWriter(RedisHashCacheWriter cacheWriter) {

			Assert.notNull(cacheWriter, "CacheWriter must not be null!");

			return new RedisHashCacheManagerBuilder(cacheWriter);
		}

		/**
		 * Define a default {@link RedisCacheConfiguration} applied to dynamically created {@link RedisCache}s.
		 *
		 * @param defaultCacheConfiguration must not be {@literal null}.
		 * @return this {@link RedisHashCacheManagerBuilder}.
		 */
		public RedisHashCacheManagerBuilder cacheDefaults(RedisCacheConfiguration defaultCacheConfiguration) {

			Assert.notNull(defaultCacheConfiguration, "DefaultCacheConfiguration must not be null!");

			this.defaultCacheConfiguration = defaultCacheConfiguration;

			return this;
		}

		/**
		 * Configure a {@link RedisHashCacheWriter}.
		 *
		 * @param cacheWriter must not be {@literal null}.
		 * @return this {@link RedisHashCacheManagerBuilder}.
		 * @since 2.3
		 */
		public RedisHashCacheManagerBuilder cacheWriter(RedisHashCacheWriter cacheWriter) {

			Assert.notNull(cacheWriter, "CacheWriter must not be null!");

			this.cacheWriter = cacheWriter;

			return this;
		}

		/**
		 * Enable {@link RedisCache}s to synchronize cache put/evict operations with ongoing Spring-managed transactions.
		 *
		 * @return this {@link RedisHashCacheManagerBuilder}.
		 */
		public RedisHashCacheManagerBuilder transactionAware() {

			this.enableTransactions = true;

			return this;
		}

		/**
		 * Append a {@link Set} of cache names to be pre initialized with current {@link RedisCacheConfiguration}.
		 * <strong>NOTE:</strong> This calls depends on {@link #cacheDefaults(RedisCacheConfiguration)} using whatever
		 * default {@link RedisCacheConfiguration} is present at the time of invoking this method.
		 *
		 * @param cacheNames must not be {@literal null}.
		 * @return this {@link RedisHashCacheManagerBuilder}.
		 */
		public RedisHashCacheManagerBuilder initialCacheNames(Set<String> cacheNames) {

			Assert.notNull(cacheNames, "CacheNames must not be null!");

			cacheNames.forEach(it -> withCacheConfiguration(it, defaultCacheConfiguration));
			return this;
		}

		/**
		 * Append a {@link Map} of cache name/{@link RedisCacheConfiguration} pairs to be pre initialized.
		 *
		 * @param cacheConfigurations must not be {@literal null}.
		 * @return this {@link RedisHashCacheManagerBuilder}.
		 */
		public RedisHashCacheManagerBuilder withInitialCacheConfigurations(
				Map<String, RedisCacheConfiguration> cacheConfigurations) {

			Assert.notNull(cacheConfigurations, "CacheConfigurations must not be null!");
			cacheConfigurations.forEach((cacheName, configuration) -> Assert.notNull(configuration,
					String.format("RedisCacheConfiguration for cache %s must not be null!", cacheName)));

			this.initialCaches.putAll(cacheConfigurations);
			return this;
		}

		/**
		 * @param cacheName             cacheName
		 * @param cacheConfiguration    cacheConfiguration
		 * @return this {@link RedisHashCacheManagerBuilder}.
		 * @since 2.2
		 */
		@SuppressWarnings("UnusedReturnValue")
		public RedisHashCacheManagerBuilder withCacheConfiguration(String cacheName,
		                                                           RedisCacheConfiguration cacheConfiguration) {

			Assert.notNull(cacheName, "CacheName must not be null!");
			Assert.notNull(cacheConfiguration, "CacheConfiguration must not be null!");

			this.initialCaches.put(cacheName, cacheConfiguration);
			return this;
		}

		/**
		 * Disable in-flight {@link org.springframework.cache.Cache} creation for unconfigured caches.
		 * <p />
		 * {@link RedisHashCacheManager#getMissingCache(String)} returns {@literal null} for any unconfigured
		 * {@link org.springframework.cache.Cache} instead of a new {@link RedisCache} instance. This allows eg.
		 * {@link org.springframework.cache.support.CompositeCacheManager} to chime in.
		 *
		 * @return this {@link RedisHashCacheManagerBuilder}.
		 * @since 2.0.4
		 */
		public RedisHashCacheManagerBuilder disableCreateOnMissingCache() {

			this.allowInFlightCacheCreation = false;
			return this;
		}

		/**
		 * Get the {@link Set} of cache names for which the builder holds {@link RedisCacheConfiguration configuration}.
		 *
		 * @return an unmodifiable {@link Set} holding the name of caches for which a {@link RedisCacheConfiguration
		 *         configuration} has been set.
		 * @since 2.2
		 */
		public Set<String> getConfiguredCaches() {
			return Collections.unmodifiableSet(this.initialCaches.keySet());
		}

		/**
		 * Get the {@link RedisCacheConfiguration} for a given cache by its name.
		 *
		 * @param cacheName must not be {@literal null}.
		 * @return {@link Optional#empty()} if no {@link RedisCacheConfiguration} set for the given cache name.
		 * @since 2.2
		 */
		public Optional<RedisCacheConfiguration> getCacheConfigurationFor(String cacheName) {
			return Optional.ofNullable(this.initialCaches.get(cacheName));
		}

		/**
		 * Create new instance of {@link RedisHashCacheManager} with configuration options applied.
		 *
		 * @return new instance of {@link RedisHashCacheManager}.
		 */
		public RedisHashCacheManager build() {

			Assert.state(cacheWriter != null, "CacheWriter must not be null! You can provide one via 'RedisCacheManagerBuilder#cacheWriter(RedisHashCacheWriter)'.");

			RedisHashCacheManager cm = new RedisHashCacheManager(cacheWriter, defaultCacheConfiguration, initialCaches,
			                                                     allowInFlightCacheCreation);

			cm.setTransactionAware(enableTransactions);

			return cm;
		}
	}
}