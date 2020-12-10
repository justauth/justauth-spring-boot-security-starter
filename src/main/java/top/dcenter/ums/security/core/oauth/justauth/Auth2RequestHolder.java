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
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.request.AuthDefaultRequest;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import top.dcenter.ums.security.core.oauth.justauth.cache.AuthStateRedisCache;
import top.dcenter.ums.security.core.oauth.justauth.cache.AuthStateSessionCache;
import top.dcenter.ums.security.core.oauth.justauth.enums.StateCacheType;
import top.dcenter.ums.security.core.oauth.justauth.request.Auth2DefaultRequest;
import top.dcenter.ums.security.core.oauth.justauth.request.AuthCustomizeRequest;
import top.dcenter.ums.security.core.oauth.justauth.request.AuthDefaultRequestAdapter;
import top.dcenter.ums.security.core.oauth.justauth.source.AuthCustomizeSource;
import top.dcenter.ums.security.core.oauth.justauth.source.AuthGitlabPrivateSource;
import top.dcenter.ums.security.core.oauth.properties.Auth2Properties;
import top.dcenter.ums.security.core.oauth.properties.BaseAuth2Properties;
import top.dcenter.ums.security.core.oauth.properties.JustAuthProperties;
import top.dcenter.ums.security.core.oauth.util.MvcUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static top.dcenter.ums.security.core.oauth.consts.SecurityConstants.URL_SEPARATOR;

/**
 * JustAuth内置的各api需要的url， 用枚举类分平台类型管理
 *
 * @author YongWu zheng
 * @version V1.0  Created by 2020-10-06 18:09
 */
@Slf4j
public final class Auth2RequestHolder implements InitializingBean, ApplicationContextAware {

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
     * key 为 providerId, value 为 {@link Auth2DefaultRequest} 的子类对象
     */
    private static final Map<String, Auth2DefaultRequest> PROVIDER_ID_AUTH_REQUEST_MAP = new ConcurrentHashMap<>();

    /**
     * key 为 {@link AuthSource}, value 为 providerId
     */
    private static final Map<AuthSource, String> SOURCE_PROVIDER_ID_MAP = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;

    /**
     * 自定义 OAuth2 Login source, 应用启动时自动注入, 如果未实现此为 null 值,
     * 注意: {@link AuthCustomizeSource} 与 {@link AuthCustomizeRequest} 必须同时实现.
     */
    private volatile static AuthCustomizeSource authCustomizeSource = null;
    /**
     * 自定义 OAuth2 Login source, 应用启动时自动注入, 如果未实现此为 null 值,
     * 注意: {@link AuthGitlabPrivateSource} 与 {@link AuthCustomizeRequest} 必须同时实现.
     */
    private volatile static AuthGitlabPrivateSource authGitlabPrivateSource = null;

    /**
     * 当 {@link #authCustomizeSource} 为 null 时, authCustomizeSource 将会被设置到  {@link #authCustomizeSource}; 否则什么都不做.
     * @param authCustomizeSource auth2 Customize Source
     */
    public static synchronized void setAuthCustomizeSource(AuthCustomizeSource authCustomizeSource) {
        if (Auth2RequestHolder.authCustomizeSource == null) {
            Auth2RequestHolder.authCustomizeSource = authCustomizeSource;
        }
    }

    /**
     * 当 {@link #authGitlabPrivateSource} 为 null 时, authGitlabPrivateSource 将会被设置到  {@link #authGitlabPrivateSource}; 否则什么都不做.
     * @param authGitlabPrivateSource   auth2 GitlabPrivate Source
     */
    public static synchronized void setAuthGitlabPrivateSource(AuthGitlabPrivateSource authGitlabPrivateSource) {
        if (Auth2RequestHolder.authGitlabPrivateSource == null) {
            Auth2RequestHolder.authGitlabPrivateSource = authGitlabPrivateSource;
        }
    }

    /**
     * 根据 providerId 获取 {@link Auth2DefaultRequest}
     * @param providerId    providerId
     * @return  返回 {@link Auth2DefaultRequest},  当没有对应的 {@link Auth2DefaultRequest} 时, 返回 null
     */
    @Nullable
    public static Auth2DefaultRequest getAuth2DefaultRequest(String providerId) {
        if (PROVIDER_ID_AUTH_REQUEST_MAP.size() < 1 || providerId == null)
        {
            return null;
        }
        return PROVIDER_ID_AUTH_REQUEST_MAP.get(providerId);
    }

    /**
     * 根据 {@link AuthSource} 获取 providerId
     * @param source    {@link AuthSource}
     * @return  返回 providerId, 没有对应的第三方则返回 null
     */
    public static String getProviderId(AuthSource source) {
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
    public static Collection<String> getValidProviderIds() {
        return Collections.unmodifiableCollection(PROVIDER_ID_AUTH_REQUEST_MAP.keySet());
    }

    /**
     * 获取有效的 providerIds
     * @return  有效的 providerId Set
     */
    public static Collection<String> getAllProviderIds() {
        return Collections.unmodifiableCollection(SOURCE_PROVIDER_ID_MAP.values());
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        try {
            Auth2RequestHolder.setAuthCustomizeSource(applicationContext.getBean(AuthCustomizeSource.class));
        }
        catch (Exception e) {
            log.info("没有自定义实现 {}", AuthCustomizeSource.class.getName());
        }

        try {
            Auth2RequestHolder.setAuthGitlabPrivateSource(applicationContext.getBean(AuthGitlabPrivateSource.class));
        }
        catch (Exception e) {
            log.info("没有自定义实现 {}", AuthGitlabPrivateSource.class.getName());
        }

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
                AuthSource source = null;
                try {
                    source = AuthDefaultSource.valueOf(String.join(FIELD_SEPARATOR, splits).toUpperCase());
                    SOURCE_PROVIDER_ID_MAP.put(source, providerId);
                }
                catch (Exception e) {
                    if (Auth2RequestHolder.authCustomizeSource != null
                            && getProviderIdBySource(Auth2RequestHolder.authCustomizeSource).equals(providerId)) {
                        source = Auth2RequestHolder.authCustomizeSource;
                        SOURCE_PROVIDER_ID_MAP.put(Auth2RequestHolder.authCustomizeSource, providerId);
                    }
                    else if (Auth2RequestHolder.authGitlabPrivateSource != null
                            && getProviderIdBySource(Auth2RequestHolder.authGitlabPrivateSource).equals(providerId)) {
                        source = Auth2RequestHolder.authGitlabPrivateSource;
                        SOURCE_PROVIDER_ID_MAP.put(Auth2RequestHolder.authCustomizeSource, providerId);
                    }

                }

                BaseAuth2Properties baseAuth2Properties = ((BaseAuth2Properties) baseProperties);
                if (baseAuth2Properties.getClientId() != null && baseAuth2Properties.getClientSecret() != null)
                {
                    if (source == null) {
                        throw new RuntimeException(String.format("获取不到 %s 相对应的 me.zhyd.oauth.config.AuthSource",
                                                                 providerId));
                    }

                    Auth2DefaultRequest auth2DefaultRequest = getAuth2DefaultRequest(source, auth2Properties, authStateCache);
                    if (null != auth2DefaultRequest) {
                        PROVIDER_ID_AUTH_REQUEST_MAP.put(providerId, auth2DefaultRequest);
                    }
                }
            }
        }

    }

    /**
     * 获取 {@link Auth2DefaultRequest}
     *
     * @return {@link Auth2DefaultRequest}
     */
    @Nullable
    private Auth2DefaultRequest getAuth2DefaultRequest(@NonNull AuthSource source,
                                                       @NonNull Auth2Properties auth2Properties,
                                                       @NonNull AuthStateCache authStateCache) throws IllegalAccessException, ClassNotFoundException {

        JustAuthProperties justAuth = auth2Properties.getJustAuth();
        AuthConfig config = getAuthConfig(auth2Properties, source);
        // 设置自定义 scopes
        List<String> scopes = getScopesBySource(justAuth.getScopes(), source);
        config.setScopes(scopes);
        // 设置是否启用代理
        Auth2Properties.HttpConfigProperties proxy = auth2Properties.getProxy();
        config.setHttpConfig(proxy.getHttpConfig());
        // 设置是否忽略 state 检测
        config.setIgnoreCheckState(justAuth.getIgnoreCheckState());

        if (source instanceof AuthCustomizeSource || source instanceof AuthGitlabPrivateSource) {
            return this.getAuthDefaultRequestAdapter(config, source, authStateCache);
        }

        if (!(source instanceof AuthDefaultSource)) {
            return null;
        }

        // 是否支持第三方授权登录
        boolean isNotSupport = false;

        //noinspection AlibabaSwitchStatement
        switch ((AuthDefaultSource) source) {
            case CODING:
                config.setCodingGroupName(auth2Properties.getCoding().getCodingGroupName());
                break;
            case ALIPAY:
                config.setAlipayPublicKey(auth2Properties.getAlipay().getAlipayPublicKey());
                break;
            case QQ:
                config.setUnionId(auth2Properties.getQq().getUnionId());
                break;
            case WECHAT_ENTERPRISE:
                config.setAgentId(auth2Properties.getWechatEnterprise().getAgentId());
                break;
            case STACK_OVERFLOW:
                config.setStackOverflowKey(auth2Properties.getStackOverflow().getStackOverflowKey());
            case GITHUB: case GOOGLE: case FACEBOOK: case MICROSOFT: case PINTEREST: case GITLAB: case TWITTER:
                config.getHttpConfig().setTimeout((int) proxy.getForeignTimeout().toMillis());
                break;
            case CSDN: case FEISHU:
                isNotSupport = true;
                break;
            default:
        }

        if (isNotSupport) {
            return null;
        }
        return this.getAuthDefaultRequestAdapter(config, source, authStateCache);
    }

    /**
     * 根据 source 获取对应的自定义 scopes
     * @param scopes    用户自定义的所有第三方的 scopes, 格式: source:scope 如: [QQ:write, QQ:read, GITEE:email, GITHUB:read]
     * @param source    {@link AuthSource}
     * @return 返回 source 相对应的 scopes
     */
    @NonNull
    private List<String> getScopesBySource(@Nullable List<String> scopes, @NonNull AuthSource source) {
        if (CollectionUtils.isEmpty(scopes)) {
            return new ArrayList<>(0);
        }
        final String sourceName = source.getName() + ":";

        return scopes.stream()
                     .filter(scope -> scope.startsWith(sourceName))
                     .map(scope -> scope.substring(sourceName.length()))
                     .collect(Collectors.toList());
    }

    /**
     * 根据 {@link AuthSource} 获取对应的 {@link Auth2Properties} 字段名称(即 providerId)
     * @param source    {@link AuthSource}
     * @return  {@link AuthSource} 对应的 {@link Auth2Properties} 字段名称(即 providerId)
     */
    @SuppressWarnings("unused")
    public static String getProviderIdBySource(@NonNull AuthSource source) {
        String[] splits = source.getName().split(FIELD_SEPARATOR);
        return toProviderId(splits);
    }

    /**
     * 获取 {@link AuthDefaultRequest} 的适配器
     * @param config                {@link AuthDefaultRequest} 的 {@link AuthConfig}
     * @param source                {@link AuthDefaultRequest} 的 {@link AuthSource}
     * @param authStateCache        {@link AuthDefaultRequest} 的 {@link AuthStateCache}
     * @return                      {@link AuthDefaultRequest} 相对应的适配器
     */
    @NonNull
    private AuthDefaultRequestAdapter getAuthDefaultRequestAdapter(@NonNull AuthConfig config,
                                                                   @NonNull AuthSource source,
                                                                   @NonNull AuthStateCache authStateCache) throws ClassNotFoundException {
        final AuthDefaultRequestAdapter adapter = new AuthDefaultRequestAdapter(config, source, authStateCache);
        Class<?>[] argumentTypes = new Class[]{AuthConfig.class, AuthStateCache.class};
        Object[] arguments = new Object[]{config, authStateCache};
        final AuthDefaultRequest proxyObject = createProxy(getAuthRequestClassBySource(source),
                                                           argumentTypes, arguments, adapter);
        adapter.setAuthDefaultRequest(proxyObject);
        return adapter;
    }

    /**
     * 创建 {@code targetClass} 的代理对象, 主要是替换 {@link AuthDefaultRequest} 的 {@code getRealState(state)} 方法
     * 为 {@link AuthDefaultRequestAdapter#getRealState(String)} 方法.
     * @param targetClass       代理的目标对象 Class, 必须是 {@link AuthDefaultRequest} 的子类 Class
     * @param argumentTypes     目标对象构造参数类型数组
     * @param arguments         目标对象构造参数值数组与 argumentTypes 一一对应
     * @param adapter           {@link AuthDefaultRequestAdapter}
     * @return                  targetClass 的代理对象
     */
    @NonNull
    private AuthDefaultRequest createProxy(Class<?> targetClass,
                                           Class<?>[] argumentTypes,
                                           Object[] arguments,
                                           AuthDefaultRequestAdapter adapter) throws ClassNotFoundException {

        if (!AuthDefaultRequest.class.isAssignableFrom(targetClass)) {
            throw new ClassNotFoundException(targetClass.getName() + " 必须是 me.zhyd.oauth.request.AuthDefaultRequest 的子类");
        }

        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setCallback((MethodInterceptor) (target, method, args, methodProxy) -> {
            // 替换 AuthDefaultRequest 的 getRealState(state) 方法为 AuthDefaultRequestAdapter 的 getRealState(state) 方法
            if (target instanceof AuthDefaultRequest && !(target instanceof AuthDefaultRequestAdapter)
                    && "getRealState".equals(method.getName())) {
                return adapter.getRealState((String) args[0]);
            }
            return methodProxy.invokeSuper(target, args);
        });

        return (AuthDefaultRequest) enhancer.create(argumentTypes, arguments);
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
     * @param source            {@link AuthSource}
     * @return  返回 {@link AuthConfig} 对象
     * @throws IllegalAccessException   IllegalAccessException
     * @throws NullPointerException     NullPointerException
     */
    private AuthConfig getAuthConfig(@NonNull Auth2Properties auth2Properties,
                                     @NonNull AuthSource source) throws IllegalAccessException, NullPointerException {
        AuthConfig.AuthConfigBuilder builder = AuthConfig.builder();

        // 根据 AuthDefaultSource 获取对应的 Auth2Properties 字段名称(即providerId)
        String providerId = getProviderId(source);

        // 获取字段 fieldName(即providerId) 的值
        Class<? extends Auth2Properties> aClass = auth2Properties.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        // 第三方属性(providerId,clientId, clientSecret)对象
        Object providerProperties = null;
        for (Field field : declaredFields)
        {
            field.setAccessible(true);
            if (field.getName().equals(providerId))
            {
                providerProperties = field.get(auth2Properties);
                break;
            }
        }

        requireNonNull(providerProperties, String.format("获取不到 %s 类型所对应的 BaseAuth2Properties 的子类", source.getName()));

        // 设置 clientId 与 clientSecret
        Class<BaseAuth2Properties> baseClass = BaseAuth2Properties.class;
        declaredFields = baseClass.getDeclaredFields();
        for (Field field : declaredFields)
        {
            field.setAccessible(true);
            if (CLIENT_ID_FIELD_NAME.equals(field.getName()))
            {
                String clientId = (String) field.get(providerProperties);
                requireNonNull(clientId, String.format("获取不到 %s 类型所对应的 %s 的值", source.getName(), CLIENT_ID_FIELD_NAME));
                builder.clientId(clientId);
            }
            if (CLIENT_SECRET_FIELD_NAME.equals(field.getName()))
            {
                String clientSecret = (String) field.get(providerProperties);
                requireNonNull(clientSecret, String.format("获取不到 %s 类型所对应的 %s 的值", source.getName(), CLIENT_SECRET_FIELD_NAME));
                builder.clientSecret(clientSecret);
            }
        }

        // 构建 redirectUri
        String redirectUri = auth2Properties.getDomain() + MvcUtil.getServletContextPath()
                + auth2Properties.getRedirectUrlPrefix() + URL_SEPARATOR + providerId;

        return builder.redirectUri(redirectUri).build();
    }

    /**
     * {@link AuthDefaultRequest} 子类的包名
     */
    public static final String AUTH_REQUEST_PACKAGE = "me.zhyd.oauth.request.";
    /**
     * {@link AuthDefaultRequest} 子类类名前缀
     */
    public static final String AUTH_REQUEST_PREFIX = "Auth";
    /**
     * {@link AuthDefaultRequest} 子类类名后缀
     */
    public static final String AUTH_REQUEST_SUFFIX = "Request";

    /**
     * 根据 {@link AuthSource} 获取对应的 {@link AuthDefaultRequest} 子类的 Class
     * @param source    {@link AuthSource}
     * @return  返回 {@link AuthSource} 对应的 {@link AuthDefaultRequest} 子类的 Class
     */
    @NonNull
    public static Class<?> getAuthRequestClassBySource(@NonNull AuthSource source) throws ClassNotFoundException {
        if (source instanceof AuthCustomizeSource) {
            if (Auth2RequestHolder.authCustomizeSource == null) {
                throw new RuntimeException("必须实现 top.dcenter.ums.security.core.oauth.justauth.source.AuthCustomizeSource 且注入 IOC 容器");
            }
            return Auth2RequestHolder.authCustomizeSource.getCustomizeRequestClass();
        }

        if (source instanceof AuthGitlabPrivateSource) {
            if (Auth2RequestHolder.authGitlabPrivateSource == null) {
                throw new RuntimeException("必须实现 top.dcenter.ums.security.core.oauth.justauth.source.AuthCustomizeSource 且注入 IOC 容器");
            }
            return Auth2RequestHolder.authGitlabPrivateSource.getCustomizeRequestClass();
        }

        if (!(source instanceof AuthDefaultSource)) {
            throw new RuntimeException("AuthSource 必须是 me.zhyd.oauth.config.AuthDefaultSource 或 top.dcenter.ums.security.core.oauth.justauth.source.AuthCustomizeSource 子类");
        }

        String[] splits = ((AuthDefaultSource) source).name().split(FIELD_SEPARATOR);
        String authRequestClassName = AUTH_REQUEST_PACKAGE + toAuthRequestClassName(splits);
        return Class.forName(authRequestClassName);
    }

    /**
     * 根据传入的字符串数组转换为类名格式的字符串, 另外 DingTalk -> DingTalk, WECHAT -> WeChat.
     * @param splits    字符串数组, 例如: [WECHAT, OPEN]
     * @return  返回类名格式的字符串, 如传入的数组是: [STACK, OVERFLOW] 那么返回 AuthStackOverflowRequest
     */
    @NonNull
    private static String toAuthRequestClassName(String[] splits) {
        StringBuilder sb = new StringBuilder();
        sb.append(AUTH_REQUEST_PREFIX);
        for (String split : splits) {
            split = split.toLowerCase();
            if (AuthDefaultSource.DINGTALK.name().equalsIgnoreCase(split)) {
                sb.append("DingTalk");
                continue;
            }
            if ("wechat".equalsIgnoreCase(split)) {
                sb.append("WeChat");
                continue;
            }
            if (split.length() > 1) {
                sb.append(split.substring(0, 1).toUpperCase()).append(split.substring(1));
            }
            else {
                sb.append(split.toUpperCase());
            }
        }
        sb.append(AUTH_REQUEST_SUFFIX);
        return sb.toString();
    }

    /**
     * 根据传入的字符串数组转换为驼峰格式的字符串
     * @param splits    字符串数组, 例如: [WECHAT, OPEN]
     * @return  驼峰格式的字符串, 如传入的数组是: [WECHAT, OPEN] 那么返回 wechatOpen
     */
    @NonNull
    private static String toProviderId(String[] splits) {
        if (splits.length == 1) {
            return splits[0].trim().toLowerCase();
        }
        StringBuilder sb = new StringBuilder();
        for (String split : splits) {
            split = split.toLowerCase();
            if (split.length() > 1) {
                sb.append(split.substring(0, 1).toUpperCase()).append(split.substring(1));
            }
            else {
                sb.append(split.toUpperCase());
            }
        }
        String firstChar = String.valueOf(sb.charAt(0)).toLowerCase();
        sb.replace(0, 1, firstChar);
        return sb.toString();
    }

}