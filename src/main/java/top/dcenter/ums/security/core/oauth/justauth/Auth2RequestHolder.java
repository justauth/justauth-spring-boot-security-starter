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

package top.dcenter.ums.security.core.oauth.justauth;

import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.cache.AuthDefaultStateCache;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthDefaultSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import top.dcenter.ums.security.core.oauth.justauth.cache.AuthStateRedisCache;
import top.dcenter.ums.security.core.oauth.justauth.cache.AuthStateSessionCache;
import top.dcenter.ums.security.core.oauth.justauth.enums.StateCacheType;
import top.dcenter.ums.security.core.oauth.justauth.request.*;
import top.dcenter.ums.security.core.oauth.properties.Auth2Properties;
import top.dcenter.ums.security.core.oauth.properties.BaseAuth2Properties;
import top.dcenter.ums.security.core.oauth.properties.JustAuthProperties;
import top.dcenter.ums.security.core.oauth.util.MvcUtil;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;
import static top.dcenter.ums.security.core.oauth.consts.SecurityConstants.URL_SEPARATOR;

/**
 * JustAuth内置的各api需要的url， 用枚举类分平台类型管理
 *
 * @author YongWu zheng
 * @version V1.0  Created by 2020-10-06 18:09
 */
@Slf4j
public class Auth2RequestHolder implements InitializingBean, ApplicationContextAware {

    /**
     * 字段分隔符
     */
    private static final String FIELD_SEPARATOR = "_";
    /**
     * CLIENT_ID_FIELD_NAME
     */
    private static final String CLIENT_ID_FIELD_NAME = "clientId";
    /**
     * CLIENT_SECRET_FIELD_NAME
     */
    private static final String CLIENT_SECRET_FIELD_NAME = "clientSecret";
    /**
     * PROVIDER_ID_FIELD_NAME
     */
    private static final String PROVIDER_ID_FIELD_NAME = "providerId";

    /**
     * key 为 providerId, value 为 {@link Auth2DefaultRequest} 的子类对象
     */
    private static final Map<String, Auth2DefaultRequest> PROVIDER_ID_AUTH_REQUEST_MAP = new ConcurrentHashMap<>();

    /**
     * key 为 {@link AuthDefaultSource}, value 为 providerId
     */
    private static final Map<AuthDefaultSource, String> SOURCE_PROVIDER_ID_MAP = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;

    /**
     * 根据 providerId 获取 {@link Auth2DefaultRequest}
     * @param providerId    providerId
     * @return  {@link Auth2DefaultRequest}
     */
    public static Auth2DefaultRequest getAuth2DefaultRequest(String providerId) {
        if (PROVIDER_ID_AUTH_REQUEST_MAP.size() < 1 || providerId == null)
        {
            return null;
        }
        return PROVIDER_ID_AUTH_REQUEST_MAP.get(providerId);
    }

    /**
     * 根据 {@link AuthDefaultSource} 获取 providerId
     * @param source    {@link AuthDefaultSource}
     * @return  providerId
     */
    public static String getProviderId(AuthDefaultSource source) {
        if (SOURCE_PROVIDER_ID_MAP.size() < 1 || null == source)
        {
            return null;
        }
        return SOURCE_PROVIDER_ID_MAP.get(source);
    }

    /**
     * 获取有效的 providerIds
     * @return  有效的 providerId Set
     */
    public static Collection<String> getProviderIds() {
        return SOURCE_PROVIDER_ID_MAP.values();
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        // 获取 auth2Properties
        Auth2Properties auth2Properties = applicationContext.getBean(Auth2Properties.class);

        JustAuthProperties justAuthProperties = auth2Properties.getJustAuth();
        StateCacheType stateCacheType = justAuthProperties.getCacheType();


        // 获取 stateCache
        AuthStateCache authStateCache;
        if (stateCacheType.equals(StateCacheType.REDIS)) {
            final Class<?> stringRedisTemplateClass = Class.forName("org.springframework.data.redis.core.StringRedisTemplate");
            Object stringRedisTemplate = applicationContext.getBean(stringRedisTemplateClass);
            authStateCache = getAuthStateCache(stateCacheType, auth2Properties, stringRedisTemplate);
        }
        else {
            authStateCache = getAuthStateCache(stateCacheType, auth2Properties, null);
        }

        /* 获取 Auth2Properties 对象的字段与对应的值:
         *  1. 以此获取所有 BaseAuth2Properties 子类字段及对应的 providerId, AuthDefaultSource, 并存储再 SOURCE_PROVIDER_ID_MAP 中;
         *  2. 以此获取所有 BaseAuth2Properties 子类对象, 检查其字段是否带有有效的 clientId 与 clientSecret 值,
         *     如果有效, 则存储再 PROVIDER_ID_AUTH_REQUEST_MAP 中.
         */
        Class<Auth2Properties> aClass = Auth2Properties.class;
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field field : declaredFields)
        {
            field.setAccessible(true);
            Object baseProperties = field.get(auth2Properties);
            if (baseProperties instanceof BaseAuth2Properties)
            {
                String providerId = field.getName();
                String[] splits = MvcUtil.splitByCharacterTypeCamelCase(providerId, true);
                AuthDefaultSource source = AuthDefaultSource.valueOf(String.join(FIELD_SEPARATOR, splits).toUpperCase());

                SOURCE_PROVIDER_ID_MAP.put(source, providerId);

                BaseAuth2Properties baseAuth2Properties = ((BaseAuth2Properties) baseProperties);
                if (baseAuth2Properties.getClientId() != null && baseAuth2Properties.getClientSecret() != null)
                {
                    Auth2DefaultRequest auth2DefaultRequest = getAuth2DefaultRequest(source, auth2Properties,authStateCache);
                    PROVIDER_ID_AUTH_REQUEST_MAP.put(providerId, auth2DefaultRequest);
                }
            }
        }

    }

    /**
     * 获取 {@link Auth2DefaultRequest}
     *
     * @return {@link Auth2DefaultRequest}
     */
    @SuppressWarnings({"AlibabaMethodTooLong", "deprecation"})
    private Auth2DefaultRequest getAuth2DefaultRequest(@NonNull AuthDefaultSource source,
                                                       @NonNull Auth2Properties auth2Properties,
                                                       @NonNull AuthStateCache authStateCache) throws IllegalAccessException {

        JustAuthProperties justAuth = auth2Properties.getJustAuth();
        AuthConfig config = getAuthConfig(auth2Properties, source);
        // 设置自定义 scopes
        config.setScopes(justAuth.getScopes());
        // 设置是否启用代理
        Auth2Properties.HttpConfigProperties proxy = auth2Properties.getProxy();
        config.setHttpConfig(proxy.getHttpConfig());
        // 设置是否忽略 state 检测
        config.setIgnoreCheckState(justAuth.getIgnoreCheckState());

        switch (source) {
            case GITHUB:
                config.getHttpConfig().setTimeout((int) proxy.getForeignTimeout().toMillis());
                return new Auth2GithubRequest(config, authStateCache);
            case WEIBO:
                return new Auth2WeiboRequest(config, authStateCache);
            case GITEE:
                return new Auth2GiteeRequest(config, authStateCache);
            case DINGTALK:
                return new Auth2DingTalkRequest(config, authStateCache);
            case BAIDU:
                return new Auth2BaiduRequest(config, authStateCache);
            case CSDN:
                return new Auth2CsdnRequest(config, authStateCache);
            case CODING:
                config.setCodingGroupName(auth2Properties.getCoding().getCodingGroupName());
                return new Auth2CodingRequest(config, authStateCache);
            case OSCHINA:
                return new Auth2OschinaRequest(config, authStateCache);
            case ALIPAY:
                config.setAlipayPublicKey(auth2Properties.getAlipay().getAlipayPublicKey());
                return new Auth2AlipayRequest(config, authStateCache);
            case QQ:
                config.setUnionId(auth2Properties.getQq().getUnionId());
                return new Auth2QqRequest(config, authStateCache);
            case WECHAT_OPEN:
                return new Auth2WeChatOpenRequest(config, authStateCache);
            case WECHAT_MP:
                return new Auth2WeChatMpRequest(config, authStateCache);
            case TAOBAO:
                return new Auth2TaobaoRequest(config, authStateCache);
            case GOOGLE:
                config.getHttpConfig().setTimeout((int) proxy.getForeignTimeout().toMillis());
                return new Auth2GoogleRequest(config, authStateCache);
            case FACEBOOK:
                config.getHttpConfig().setTimeout((int) proxy.getForeignTimeout().toMillis());
                return new Auth2FacebookRequest(config, authStateCache);
            case DOUYIN:
                return new Auth2DouyinRequest(config, authStateCache);
            case LINKEDIN:
                return new Auth2LinkedinRequest(config, authStateCache);
            case MICROSOFT:
                config.getHttpConfig().setTimeout((int) proxy.getForeignTimeout().toMillis());
                return new Auth2MicrosoftRequest(config, authStateCache);
            case MI:
                return new Auth2MiRequest(config, authStateCache);
            case TOUTIAO:
                return new Auth2ToutiaoRequest(config, authStateCache);
            case TEAMBITION:
                return new Auth2TeambitionRequest(config, authStateCache);
            case RENREN:
                return new Auth2RenrenRequest(config, authStateCache);
            case PINTEREST:
                config.getHttpConfig().setTimeout((int) proxy.getForeignTimeout().toMillis());
                return new Auth2PinterestRequest(config, authStateCache);
            case STACK_OVERFLOW:
                config.getHttpConfig().setTimeout((int) proxy.getForeignTimeout().toMillis());
                config.setStackOverflowKey(auth2Properties.getStackOverflow().getStackOverflowKey());
                return new Auth2StackOverflowRequest(config, authStateCache);
            case HUAWEI:
                return new Auth2HuaweiRequest(config, authStateCache);
            case WECHAT_ENTERPRISE:
                config.setAgentId(auth2Properties.getWechatEnterprise().getAgentId());
                return new Auth2WeChatEnterpriseRequest(config, authStateCache);
            case KUJIALE:
                return new Auth2KujialeRequest(config, authStateCache);
            case GITLAB:
                config.getHttpConfig().setTimeout((int) proxy.getForeignTimeout().toMillis());
                return new Auth2GitlabRequest(config, authStateCache);
            case MEITUAN:
                return new Auth2MeituanRequest(config, authStateCache);
            case ELEME:
                return new Auth2ElemeRequest(config, authStateCache);
            case TWITTER:
                config.getHttpConfig().setTimeout((int) proxy.getForeignTimeout().toMillis());
                return new Auth2TwitterRequest(config, authStateCache);
            case FEISHU:
                return new Auth2FeishuRequest(config, authStateCache);
            case JD:
                return new Auth2JdRequest(config, authStateCache);
            case ALIYUN:
                return new Auth2AliyunRequest(config, authStateCache);
            default:
                return null;
        }
    }

    /**
     * 获取 {@link AuthStateCache} 对象
     * @param type                  {@link StateCacheType}
     * @param auth2Properties       auth2Properties
     * @param stringRedisTemplate   stringRedisTemplate
     * @return  {@link AuthStateCache}
     */
    private AuthStateCache getAuthStateCache(StateCacheType type, Auth2Properties auth2Properties,
                                             Object stringRedisTemplate) {
        switch(type) {
            case DEFAULT:
                return AuthDefaultStateCache.INSTANCE;
            case SESSION:
                return new AuthStateSessionCache(auth2Properties);
            case REDIS:
                if (stringRedisTemplate == null)
                {
                    throw new  RuntimeException(String.format("applicationContext 中获取不到 %s, %s 类型的缓存无法创建!",
                                                              "org.springframework.data.redis.core.StringRedisTemplate", type.name()));
                }
                return new AuthStateRedisCache(auth2Properties, stringRedisTemplate);
            default:
                log.error("{} 类型不匹配, 使用 {} 类型缓存替代",
                          StateCacheType.class.getName(), StateCacheType.DEFAULT.name());
                return AuthDefaultStateCache.INSTANCE;
        }

    }


    /**
     * 根据 auth2Properties 与 source 构建 {@link AuthConfig} 对象.
     * @param auth2Properties   auth2Properties
     * @param source            source
     * @return  返回 {@link AuthConfig} 对象
     * @throws IllegalAccessException   IllegalAccessException
     * @throws NullPointerException     NullPointerException
     */
    private AuthConfig getAuthConfig(@NonNull Auth2Properties auth2Properties,
                                     @NonNull AuthDefaultSource source) throws IllegalAccessException, NullPointerException {
        AuthConfig.AuthConfigBuilder builder = AuthConfig.builder();

        // 根据 AuthDefaultSource 获取对应的 Auth2Properties 字段名称(即providerId)
        String fieldName = getProviderId(source);

        // 获取字段 fieldName(即providerId) 的值
        Class<? extends Auth2Properties> aClass = auth2Properties.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        // 第三方属性(providerId,clientId, clientSecret)对象
        Object providerProperties = null;
        for (Field field : declaredFields)
        {
            field.setAccessible(true);
            if (field.getName().equals(fieldName))
            {
                providerProperties = field.get(auth2Properties);
                break;
            }
        }

        // 获取 providerProperties 对象中 providerId
        String providerId = "";

        requireNonNull(providerProperties, String.format("获取不到 %s 类型所对应的 BaseAuth2Properties 的子类", source.name()));

        declaredFields = providerProperties.getClass().getDeclaredFields();
        for (Field field : declaredFields)
        {
            field.setAccessible(true);
            if (PROVIDER_ID_FIELD_NAME.equals(field.getName()))
            {
                providerId = (String) field.get(providerProperties);
                requireNonNull(providerId, String.format("获取不到 %s 类型所对应的 %s 的值", source.name(), PROVIDER_ID_FIELD_NAME));
            }
        }

        // 设置 clientId 与 clientSecret
        Class<BaseAuth2Properties> baseClass = BaseAuth2Properties.class;
        declaredFields = baseClass.getDeclaredFields();
        for (Field field : declaredFields)
        {
            field.setAccessible(true);
            if (CLIENT_ID_FIELD_NAME.equals(field.getName()))
            {
                String clientId = (String) field.get(providerProperties);
                requireNonNull(clientId, String.format("获取不到 %s 类型所对应的 %s 的值", source.name(), CLIENT_ID_FIELD_NAME));
                builder.clientId(clientId);
            }
            if (CLIENT_SECRET_FIELD_NAME.equals(field.getName()))
            {
                String clientSecret = (String) field.get(providerProperties);
                requireNonNull(clientSecret, String.format("获取不到 %s 类型所对应的 %s 的值", source.name(), CLIENT_SECRET_FIELD_NAME));
                builder.clientSecret(clientSecret);
            }
        }

        // 构建 redirectUri
        String redirectUri = auth2Properties.getDomain() + MvcUtil.getServletContextPath()
                + auth2Properties.getRedirectUrlPrefix() + URL_SEPARATOR + providerId;

        return builder.redirectUri(redirectUri).build();
    }

    /**
     * 根据 {@link AuthDefaultSource} 获取对应的 {@link Auth2Properties} 字段名称(即 providerId)
     * @param source    {@link AuthDefaultSource}
     * @return  {@link AuthDefaultSource} 对应的 {@link Auth2Properties} 字段名称(即 providerId)
     */
    @SuppressWarnings("unused")
    public static String getProviderIdBySource(@NonNull AuthDefaultSource source) {
        String fieldName;
        String name = source.name().toLowerCase();
        String[] splits = name.split(FIELD_SEPARATOR);
        fieldName = name;
        if (splits.length > 1)
        {
            String secondName = splits[1];
            fieldName = splits[0] + secondName.substring(0, 1).toUpperCase() + secondName.substring(1);
        }
        return fieldName;
    }

}