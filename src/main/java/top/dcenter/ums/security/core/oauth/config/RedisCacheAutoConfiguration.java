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

package top.dcenter.ums.security.core.oauth.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.NonNull;
import org.springframework.security.jackson2.CoreJackson2Module;
import org.springframework.security.web.jackson2.WebJackson2Module;
import top.dcenter.ums.security.core.oauth.jackson.deserializes.Auth2Jackson2Module;
import top.dcenter.ums.security.core.oauth.properties.RedisCacheProperties;
import top.dcenter.ums.security.core.oauth.repository.jdbc.cache.RedisHashCacheManager;
import top.dcenter.ums.security.core.oauth.repository.jdbc.key.generator.RemoveConnectionsByConnectionKeyWithUserIdKeyGenerator;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 简单的实现 Redis cache 自定义配置 {@link CacheManager}, 向 IOC 容器中注入 beanName=socialRedisHashCacheManager 的实例. <br><br>
 * 此 redis Cache 对 {@link org.springframework.data.redis.cache.RedisCache} 进行了修改, 把缓存的 KV 格式该成了 Hash 格式.<br>
 * 1. Cacheable 操作异常处理: <br>
 *     异常处理在日志中打印出错误信息，但是放行，保证redis服务器出现连接等问题的时候不影响程序的正常运行，使得能够出问题时不用缓存. <br>
 * Cacheable 操作自定义异常处理: 实现 {@link CacheErrorHandler } 注入 IOC 容器即可自动注入 {@link CachingConfigurerSupport},
 * 当然也可自定义 {@link CachingConfigurerSupport} .<br>
 * 2. 缓存穿透: 对查询结果 null 值进行缓存, 添加时更新缓存 null 值, 或者 删除此缓存.<br>
 * 3. 取缓存 TTL 的 20% 作为动态的随机变量上下浮动, 防止同时缓存失效而缓存击穿.<br>
 * @author YongWu zheng
 * @version V2.0  Created by  2020-06-11 22:57
 */
@Configuration
@ConditionalOnProperty(prefix = "ums.cache.redis", name = "open", havingValue = "true")
@EnableCaching
@Slf4j
public class RedisCacheAutoConfiguration {

    /**
     * redis cache 解析Key：根据分隔符 "__" 来判断是否是 hash 类型
     */
    public static final String REDIS_CACHE_HASH_KEY_SEPARATE = "__";
    public static final String REDIS_CACHE_KEY_SEPARATE = ":";

    /**
     * 第三方授权登录信息 kv 缓存
     */
    public static final String USER_CONNECTION_CACHE_NAME = "UCC";
    /**
     * 第三方授权登录信息 hash 缓存, 当清除缓存时精确清除, 用按 hash key field 清除.
     */
    public static final String USER_CONNECTION_HASH_CACHE_NAME = "UCHC";
    /**
     * 第三方授权登录信息 hash 缓存, 当清除缓存时模糊清除, 用按 hash key 清除.
     */
    public static final String USER_CONNECTION_HASH_ALL_CLEAR_CACHE_NAME = "UCHACC";

    private final RedisCacheProperties redisCacheProperties;
    private final RedisProperties redisProperties;

    public RedisCacheAutoConfiguration(RedisCacheProperties redisCacheProperties, RedisProperties redisProperties) {
        this.redisCacheProperties = redisCacheProperties;
        Set<String> cacheNames = redisCacheProperties.getCache().getCacheNames();
        cacheNames.add(USER_CONNECTION_CACHE_NAME);
        cacheNames.add(USER_CONNECTION_HASH_CACHE_NAME);
        cacheNames.add(USER_CONNECTION_HASH_ALL_CLEAR_CACHE_NAME);
        this.redisProperties = redisProperties;
    }

    /**
     * 配置 Jackson2JsonRedisSerializer 序列化器，在配置 redisTemplate需要用来做k,v的
     * 序列化器
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Jackson2JsonRedisSerializer getJackson2JsonRedisSerializer(){
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer;
        jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                                 ObjectMapper.DefaultTyping.NON_FINAL);
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.registerModules(new CoreJackson2Module(), new WebJackson2Module(), new Auth2Jackson2Module());
        jackson2JsonRedisSerializer.setObjectMapper(om);
        return jackson2JsonRedisSerializer;
    }

    @SuppressWarnings({"FieldMayBeFinal", "rawtypes"})
    private static Jackson2JsonRedisSerializer jackson2JsonRedisSerializer =
            getJackson2JsonRedisSerializer();


    /**
     * 自定义 LettuceConnectionFactory
     */
    private LettuceConnectionFactory createLettuceConnectionFactory(
            int dbIndex, String hostName, int port, String password,
            int maxIdle, int minIdle, int maxActive,
            Long maxWait, Long timeOut, Duration shutdownTimeOut){

        //redis配置
        RedisConfiguration redisConfiguration = new
                RedisStandaloneConfiguration(hostName, port);
        ((RedisStandaloneConfiguration) redisConfiguration).setDatabase(dbIndex);
        ((RedisStandaloneConfiguration) redisConfiguration).setPassword(password);

        //连接池配置
        //noinspection rawtypes
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxIdle(maxIdle);
        genericObjectPoolConfig.setMinIdle(minIdle);
        genericObjectPoolConfig.setMaxTotal(maxActive);
        genericObjectPoolConfig.setMaxWaitMillis(maxWait);
        //redis客户端配置
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder
                builder =  LettucePoolingClientConfiguration.builder().
                commandTimeout(Duration.ofMillis(timeOut));

        builder.shutdownTimeout(shutdownTimeOut);
        builder.poolConfig(genericObjectPoolConfig);
        LettuceClientConfiguration lettuceClientConfiguration = builder.build();
        //根据配置和客户端配置创建连接
        LettuceConnectionFactory lettuceConnectionFactory = new
                LettuceConnectionFactory(redisConfiguration,lettuceClientConfiguration);

        lettuceConnectionFactory.afterPropertiesSet();
        return lettuceConnectionFactory;
    }

    /**
     * 缓存管理器, 当 IOC 容器中有 beanName=redisCacheManager 时会替换此实例
     * @return CacheManager
     */
    @Bean("auth2RedisHashCacheManager")
    @ConditionalOnMissingBean(name = "auth2RedisHashCacheManager")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {

        RedisCacheProperties.Cache cache = redisCacheProperties.getCache();

        // 判断是否使用 spring IOC 容器中的 LettuceConnectionFactory
        // 如果使用 spring IOC 容器中的 LettuceConnectionFactory，则要注意 cache.database-index 要与 spring.redis.database 一样。
        LettuceConnectionFactory lettuceConnectionFactory;
        if (redisCacheProperties.getUseIocRedisConnectionFactory() && redisConnectionFactory instanceof LettuceConnectionFactory)
        {
            lettuceConnectionFactory = (LettuceConnectionFactory) redisConnectionFactory;
        } else
        {
            RedisProperties.Lettuce lettuce = redisProperties.getLettuce();
            RedisProperties.Pool lettucePool = lettuce.getPool();
            lettuceConnectionFactory = createLettuceConnectionFactory
                    (cache.getDatabaseIndex(),
                     redisProperties.getHost(),
                     redisProperties.getPort(),
                     redisProperties.getPassword(),
                     lettucePool.getMaxIdle(),
                     lettucePool.getMinIdle(),
                     lettucePool.getMaxActive(),
                     lettucePool.getMaxWait().getSeconds(),
                     redisProperties.getTimeout().toMillis(),
                     lettuce.getShutdownTimeout());
        }

        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig();
        // 设置缓存管理器管理的缓存的默认过期时间
        //noinspection unchecked
        defaultCacheConfig = defaultCacheConfig.entryTtl(cache.getDefaultExpireTime())
                // 设置 key为string序列化
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 设置value为json序列化
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer));
                // 不缓存空值
                //.disableCachingNullValues()

        Set<String> cacheNames = cache.getCacheNames();

        // 对每个缓存空间应用不同的配置
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>(cacheNames.size());
        for (String cacheName : cacheNames)
        {
            configMap.put(cacheName,
                          defaultCacheConfig.entryTtl(cache.getEntryTtl()));
        }

        //noinspection UnnecessaryLocalVariable
        RedisHashCacheManager cacheManager = RedisHashCacheManager.builder(lettuceConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .initialCacheNames(cacheNames)
                .withInitialCacheConfigurations(configMap)
                .build();
        return cacheManager;
    }

    @Bean("removeConnectionsByConnectionKeyWithUserIdKeyGenerator")
    public RemoveConnectionsByConnectionKeyWithUserIdKeyGenerator removeConnectionsByConnectionKeyWithUserIdKeyGenerator() {
        return new RemoveConnectionsByConnectionKeyWithUserIdKeyGenerator();
    }

    @Bean
    @ConditionalOnMissingBean(type = {"org.springframework.cache.interceptor.CacheErrorHandler"})
    public CacheErrorHandler cacheErrorHandler() {
        /*
         * Cacheable 操作异常处理:
         * 异常处理在日志中打印出错误信息，但是放行，保证redis服务器出现连接等问题的时候不影响程序的正常运行，使得能够出问题时不用缓存
         */
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(@NonNull RuntimeException e, @NonNull Cache cache, @NonNull Object key) {
                log.error("redis异常：cacheName=[{}], key=[{}]", cache.getName(), key, e);
            }

            @Override
            public void handleCachePutError(@NonNull RuntimeException e, @NonNull Cache cache, @NonNull Object key, Object value) {
                log.error("redis异常：cacheName=[{}], key=[{}]", cache.getName(), key, e);
            }

            @Override
            public void handleCacheEvictError(@NonNull RuntimeException e, @NonNull Cache cache, @NonNull Object key) {
                log.error("redis异常：cacheName=[{}], key=[{}]", cache.getName(), key, e);
            }

            @Override
            public void handleCacheClearError(@NonNull RuntimeException e, @NonNull Cache cache) {
                log.error("redis异常：cacheName=[{}], ", cache.getName(), e);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(type = {"org.springframework.cache.annotation.CachingConfigurerSupport"})
    public CachingConfigurerSupport cachingConfigurerSupport(CacheErrorHandler cacheErrorHandler) {
        return new CustomizeCachingConfigurerSupport(cacheErrorHandler);
    }

    private static class CustomizeCachingConfigurerSupport extends CachingConfigurerSupport {

        private final CacheErrorHandler cacheErrorHandler;

        public CustomizeCachingConfigurerSupport(CacheErrorHandler cacheErrorHandler) {
            this.cacheErrorHandler = cacheErrorHandler;
        }

        @Override
        public CacheManager cacheManager() {
            return super.cacheManager();
        }

        @Override
        public CacheResolver cacheResolver() {
            return super.cacheResolver();
        }

        @Override
        public KeyGenerator keyGenerator() {
            return super.keyGenerator();
        }

        /**
         * Cacheable 操作异常处理:
         * @return  CacheErrorHandler 自定义 Cacheable 操作异常处理
         */
        @Override
        public CacheErrorHandler errorHandler() {
            return this.cacheErrorHandler;
        }
    }

}