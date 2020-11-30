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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import top.dcenter.ums.security.core.mdc.config.MdcPropertiesAutoConfiguration;
import top.dcenter.ums.security.core.mdc.filter.MdcLogFilter;
import top.dcenter.ums.security.core.mdc.properties.MdcProperties;
import top.dcenter.ums.security.core.oauth.filter.login.Auth2LoginAuthenticationFilter;
import top.dcenter.ums.security.core.oauth.filter.redirect.Auth2DefaultRequestRedirectFilter;
import top.dcenter.ums.security.core.oauth.properties.Auth2Properties;
import top.dcenter.ums.security.core.oauth.provider.Auth2LoginAuthenticationProvider;
import top.dcenter.ums.security.core.oauth.repository.UsersConnectionRepository;
import top.dcenter.ums.security.core.oauth.service.Auth2StateCoder;
import top.dcenter.ums.security.core.oauth.service.Auth2UserService;
import top.dcenter.ums.security.core.oauth.service.UmsUserDetailsService;
import top.dcenter.ums.security.core.oauth.signup.ConnectionService;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

/**
 * 添加 OAuth2(JustAuth) 配置
 * @author YongWu zheng
 * @version V2.0  Created by 2020/10/12 12:31
 */
@SuppressWarnings("jol")
@Configuration
@AutoConfigureAfter({Auth2AutoConfiguration.class, MdcPropertiesAutoConfiguration.class})
public class Auth2AutoConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> implements InitializingBean {

    private final Auth2Properties auth2Properties;
    private final MdcProperties mdcProperties;
    private final UmsUserDetailsService umsUserDetailsService;
    private final Auth2UserService auth2UserService;
    private final UsersConnectionRepository usersConnectionRepository;
    private final ConnectionService connectionSignUp;
    private final ExecutorService updateConnectionTaskExecutor;
    @SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection"})
    @Autowired(required = false)
    private Auth2StateCoder auth2StateCoder;
    @SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection"})
    @Autowired(required = false)
    private AuthenticationFailureHandler authenticationFailureHandler;
    @SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection"})
    @Autowired(required = false)
    private AuthenticationSuccessHandler authenticationSuccessHandler;
    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false)
    private RememberMeServices rememberMeServices;
    @SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection"})
    @Autowired(required = false)
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;

    public Auth2AutoConfigurer(Auth2Properties auth2Properties, MdcProperties mdcProperties,
                               UmsUserDetailsService umsUserDetailsService,
                               Auth2UserService auth2UserService, UsersConnectionRepository usersConnectionRepository,
                               ConnectionService connectionSignUp,
                               @Qualifier("updateConnectionTaskExecutor") ExecutorService updateConnectionTaskExecutor) {
        this.auth2Properties = auth2Properties;
        this.mdcProperties = mdcProperties;
        this.umsUserDetailsService = umsUserDetailsService;
        this.auth2UserService = auth2UserService;
        this.usersConnectionRepository = usersConnectionRepository;
        this.connectionSignUp = connectionSignUp;
        this.updateConnectionTaskExecutor = updateConnectionTaskExecutor;
    }

    @Override
    public void configure(HttpSecurity http) {

        if (this.mdcProperties.getEnable()) {
            // 基于 MDC 机制实现日志的链路追踪过滤器
            http.addFilterBefore(new MdcLogFilter(this.mdcProperties), WebAsyncManagerIntegrationFilter.class);
        }

        // 添加第三方登录入口过滤器
        String authorizationRequestBaseUri = auth2Properties.getAuthLoginUrlPrefix();
        Auth2DefaultRequestRedirectFilter auth2DefaultRequestRedirectFilter =
                new Auth2DefaultRequestRedirectFilter(authorizationRequestBaseUri, this.auth2StateCoder,
                                                      this.authenticationFailureHandler);
        http.addFilterAfter(auth2DefaultRequestRedirectFilter, AbstractPreAuthenticatedProcessingFilter.class);

        // 添加第三方登录回调接口过滤器
        String filterProcessesUrl = auth2Properties.getRedirectUrlPrefix();
        Auth2LoginAuthenticationFilter auth2LoginAuthenticationFilter =
                new Auth2LoginAuthenticationFilter(filterProcessesUrl, auth2Properties.getSignUpUrl(), authenticationDetailsSource);
        AuthenticationManager sharedObject = http.getSharedObject(AuthenticationManager.class);
        auth2LoginAuthenticationFilter.setAuthenticationManager(sharedObject);
        if (authenticationFailureHandler != null)
        {
            // 添加认证失败处理器
            auth2LoginAuthenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
        }
        if (authenticationSuccessHandler != null)
        {
            // 添加认证成功处理器
            auth2LoginAuthenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        }

        // 添加 RememberMeServices
        if (rememberMeServices != null)
        {
            // 添加rememberMe功能配置
            auth2LoginAuthenticationFilter.setRememberMeServices(rememberMeServices);
        }

        http.addFilterAfter(postProcess(auth2LoginAuthenticationFilter), OAuth2AuthorizationRequestRedirectFilter.class);

        // 添加 provider
        Auth2LoginAuthenticationProvider auth2LoginAuthenticationProvider = new Auth2LoginAuthenticationProvider(
                auth2UserService, connectionSignUp, umsUserDetailsService,
                usersConnectionRepository, updateConnectionTaskExecutor,
                auth2Properties.getAutoSignUp(), auth2Properties.getTemporaryUserAuthorities(),
                auth2Properties.getTemporaryUserPassword());
        http.authenticationProvider(postProcess(auth2LoginAuthenticationProvider));

    }

    /**
     * 忽略非法反射警告  适用于jdk11
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void disableAccessWarnings() {
        try {
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Object unsafe = field.get(null);

            Method putObjectVolatile = unsafeClass.getDeclaredMethod("putObjectVolatile", Object.class, long.class, Object.class);
            Method staticFieldOffset = unsafeClass.getDeclaredMethod("staticFieldOffset", Field.class);

            Class loggerClass = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field loggerField = loggerClass.getDeclaredField("logger");
            Long offset = (Long) staticFieldOffset.invoke(unsafe, loggerField);
            putObjectVolatile.invoke(unsafe, loggerClass, offset, null);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void afterPropertiesSet() {
        // 忽略非法反射警告  适用于jdk11
        if (auth2Properties.getSuppressReflectWarning())
        {
            disableAccessWarnings();
        }
    }
}