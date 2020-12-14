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

package demo.config;

import demo.handler.DemoSignUpUrlAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import top.dcenter.ums.security.core.oauth.config.Auth2AutoConfigurer;
import top.dcenter.ums.security.core.oauth.properties.Auth2Properties;

/**
 * web security config
 * @author YongWu zheng
 * @version V2.0  Created by 2020/10/18 22:39
 */
@SuppressWarnings("ALL")
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private Auth2AutoConfigurer auth2AutoConfigurer;
    @Autowired
    private Auth2Properties auth2Properties;

    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        /*
            默认为 BCryptPasswordEncoder 的实现了添加随机 salt 算法，并且能从hash后的字符串中获取 salt 进行原始密码与hash后的密码的对比
            支持格式:
            {bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
            {noop}password
            {pbkdf2}5d923b44a6d129f3ddf3e3c8d29412723dcbde72445e8ef6bf3b508fbf17fa4ed4d6b99ca763d8dc
            {scrypt}$e0801$8bWJaSu2IKSn9Z9kM+TPXfOc/9bdYSrN1oD9qfVThWEwdRTnO7re7Ei+fUZRJ68k9lTyuTeUp4of4g24hHnazw==$OAOec05+bXxvuu/1qZ6NUR+xQYvYv7BeL1QxwRpY5Pc=
            {sha256}97cde38028ad898ebc02e690819fa220e88c62e0699403e94fff291cfffaf8410849f27605abcbc0
         */
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.formLogin()
            .loginPage("/login.html")
            .successHandler(this.authenticationSuccessHandler);
        http.logout().logoutSuccessUrl("/login.html");

        http.csrf().disable();

        // ========= start: 使用 justAuth-spring-security-starter 必须步骤 =========
        // 添加 Auth2AutoConfigurer 使 OAuth2(justAuth) login 生效.
        http.apply(this.auth2AutoConfigurer);

        // 放行第三方登录入口地址与第三方登录回调地址
        // @formatter:off
        http.authorizeRequests()
                .antMatchers(HttpMethod.GET,
                             auth2Properties.getRedirectUrlPrefix() + "/*",
                             auth2Properties.getAuthLoginUrlPrefix() + "/*")
                .permitAll();
        // @formatter:on
        // ========= end: 使用 justAuth-spring-security-starter 必须步骤 =========

        http.authorizeRequests().anyRequest().permitAll();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        DemoSignUpUrlAuthenticationSuccessHandler demoSignUpUrlAuthenticationSuccessHandler = new DemoSignUpUrlAuthenticationSuccessHandler();
        demoSignUpUrlAuthenticationSuccessHandler.setDefaultTargetUrl("/index.html");
        this.authenticationSuccessHandler = demoSignUpUrlAuthenticationSuccessHandler;
        return demoSignUpUrlAuthenticationSuccessHandler;
    }

}