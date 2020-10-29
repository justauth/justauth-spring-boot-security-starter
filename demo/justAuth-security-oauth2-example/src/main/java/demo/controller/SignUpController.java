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
package demo.controller;

import com.xkcoding.http.config.HttpConfig;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import top.dcenter.ums.security.core.oauth.entity.AuthTokenPo;
import top.dcenter.ums.security.core.oauth.entity.ConnectionData;
import top.dcenter.ums.security.core.oauth.enums.ErrorCodeEnum;
import top.dcenter.ums.security.core.oauth.exception.RegisterUserFailureException;
import top.dcenter.ums.security.core.oauth.justauth.request.Auth2DefaultRequest;
import top.dcenter.ums.security.core.oauth.justauth.util.JustAuthUtil;
import top.dcenter.ums.security.core.oauth.properties.Auth2Properties;
import top.dcenter.ums.security.core.oauth.repository.UsersConnectionRepository;
import top.dcenter.ums.security.core.oauth.repository.UsersConnectionTokenRepository;
import top.dcenter.ums.security.core.oauth.service.Auth2StateCoder;
import top.dcenter.ums.security.core.oauth.service.UmsUserDetailsService;
import top.dcenter.ums.security.core.oauth.signup.ConnectionService;
import top.dcenter.ums.security.core.oauth.token.Auth2AuthenticationToken;
import top.dcenter.ums.security.core.oauth.userdetails.TemporaryUser;
import top.dcenter.ums.security.core.oauth.util.MvcUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 展示用户第一次第三方授权登录时, 不支持自动注册, 获取临时用户信息(含第三方的用户信息)的方式与自定义注册, 比如获取临时用户信息两种方式:
 * 1. 注解方式 @AuthenticationPrincipal UserDetails userDetails .<br>
 * 2. 通过 SecurityContextHolder 获取
 * <pre>
 *     final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 *     final Object principal = authentication.getPrincipal();
 *     if (principal instanceof UserDetails)
 *     {
 *         UserDetails details = ((UserDetails) principal);
 *     }
 * </pre>
 *
 * @author YongWu zheng
 * @version V2.0  Created by 2020/10/26 13:08
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Controller
public class SignUpController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * {@link HttpConfig#getTimeout()}, 单位毫秒,
     * 返回用户设置的超时时间{@link Auth2Properties.HttpConfigProperties#getTimeout()}，单位毫秒.
     */
    private final Integer timeout;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private UmsUserDetailsService umsUserDetailsService;
    @Autowired
    private UsersConnectionRepository usersConnectionRepository;
    @Autowired
    private UsersConnectionTokenRepository usersConnectionTokenRepository;
    @Autowired
    private Auth2StateCoder auth2StateCoder;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public SignUpController(Auth2Properties auth2Properties) {
        this.timeout = Math.toIntExact(auth2Properties.getProxy().getTimeout().toMillis());
    }

    @GetMapping("/signUpAgain")
    @ResponseBody
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails,
                                              @SuppressWarnings("unused") HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("userDetails", userDetails);
        map.put("securityContextHolder", SecurityContextHolder.getContext().getAuthentication());

        log.info(MvcUtil.toJsonString(userDetails));

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        final Object principal = authentication.getPrincipal();
        if (principal instanceof TemporaryUser)
        {
            TemporaryUser temporaryUser = ((TemporaryUser) principal);
            log.info(MvcUtil.toJsonString(temporaryUser));
            final AuthUser authUser = temporaryUser.getAuthUser();
            log.info(MvcUtil.toJsonString(authUser));

            // 从 request 获取前端提交数据
            // ...

            UserDetails newUserDetails;

            // 测试是否自动注册
            boolean autoSignUp = false;
            //noinspection ConstantConditions
            if (autoSignUp) {
                // 演示 1. start: 自动注册逻辑
                 newUserDetails = connectionService.signUp(authUser, authUser.getSource(), temporaryUser.getEncodeState());
                // 演示 1. end: 自动注册逻辑
            }
            else {
                // 演示 2. start: 自定义注册用户逻辑
                final String encodeState = temporaryUser.getEncodeState();
                final String authorities =
                        authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

                // 这里为第三方登录自动注册时调用，所以这里不需要实现对用户信息的注册，可以在用户登录完成后提示用户修改用户信息。
                String username = authUser.getUsername();
                // existedByUsernames(String...) usernames 生成规则. 如需自定义重新实现 generateUsernames(authUser)
                String[] usernames = umsUserDetailsService.generateUsernames(authUser);

                try {

                    // 重名检查
                    username = null;
                    final List<Boolean> existedByUserIds = umsUserDetailsService.existedByUsernames(usernames);
                    for(int i = 0, len = existedByUserIds.size(); i < len; i++) {
                        if (!existedByUserIds.get(i))
                        {
                            username = usernames[i];
                            break;
                        }
                    }
                    // 用户重名, 自动注册失败
                    if (username == null)
                    {
                        throw new RegisterUserFailureException(ErrorCodeEnum.USERNAME_USED, authUser.getUsername());
                    }

                    // 解密 encodeState  https://gitee.com/pcore/just-auth-spring-security-starter/issues/I22JC7
                    String decodeState;
                    if (this.auth2StateCoder != null) {
                        decodeState = this.auth2StateCoder.decode(encodeState);
                    }
                    else {
                        decodeState = encodeState;
                    }
                    // 注册到本地账户
                    newUserDetails = umsUserDetailsService.registerUser(authUser, username, authorities,
                                                                        decodeState);
                    // 第三方授权登录信息绑定到本地账号, 且添加第三方授权登录信息到 user_connection 与 auth_token
                    registerConnection(authUser.getSource(), authUser, newUserDetails);

                }
                catch (Exception e) {
                    log.error(String.format("OAuth2自动注册失败: error=%s, username=%s, authUser=%s",
                                            e.getMessage(), username, MvcUtil.toJsonString(authUser)), e);
                    throw new RegisterUserFailureException(ErrorCodeEnum.USER_REGISTER_FAILURE, username);
                }
                // 演示 2. end: 自定义注册用户逻辑

            }


            // 创建新的成功认证 token
            Auth2AuthenticationToken auth2AuthenticationToken =
                    new Auth2AuthenticationToken(newUserDetails, newUserDetails.getAuthorities(), authUser.getSource());
            auth2AuthenticationToken.setDetails(authentication.getDetails());
            // 更新 SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication(auth2AuthenticationToken);
            // 自己的其他更新逻辑 ...

        }

        return map;
    }

    /**
     * 第三方授权登录信息绑定到本地账号, 且添加第三方授权登录信息到 user_connection 与 auth_token
     * @param providerId    第三方服务商
     * @param authUser      {@link AuthUser}
     * @throws RegisterUserFailureException 注册失败
     */
    private void registerConnection(String providerId, AuthUser authUser, UserDetails userDetails) throws RegisterUserFailureException {

        // 注册第三方授权登录信息到 user_connection 与 auth_token
        AuthToken token = authUser.getToken();
        AuthTokenPo authToken = JustAuthUtil.getAuthTokenPo(token, providerId, this.timeout);
        // 有效期转时间戳
        Auth2DefaultRequest.expireIn2Timestamp(this.timeout, token.getExpireIn(), authToken);

        try {
            // 添加 token
            usersConnectionTokenRepository.saveAuthToken(authToken);

            // 添加到 第三方登录记录表
            addConnectionData(providerId, authUser, userDetails.getUsername(), authToken);
        }
        catch (Exception e) {
            String msg;
            if (authToken.getId() == null)
            {
                try {
                    // 再次添加 token
                    usersConnectionTokenRepository.saveAuthToken(authToken);
                    // 再次添加到 第三方登录记录表
                    addConnectionData(providerId, authUser, userDetails.getUsername(), authToken);
                }
                catch (Exception ex) {
                    msg = String.format("第三方授权登录自动注册时: 本地账户注册成功, %s, 添加第三方授权登录信息失败: %s",
                                        userDetails, MvcUtil.toJsonString(authUser));
                    log.error(msg, e);
                    throw new RegisterUserFailureException(ErrorCodeEnum.USER_REGISTER_OAUTH2_FAILURE,
                                                           ex, userDetails.getUsername());
                }
            }
            else
            {
                try {
                    // authToken 保存成功, authUser保存失败, 再次添加到 第三方登录记录表
                    addConnectionData(providerId, authUser, userDetails.getUsername(), authToken);
                }
                catch (Exception exception) {
                    msg = String.format("第三方授权登录自动注册时: 本地账户注册成功, %s, 添加第三方授权登录信息失败: %s, 但 AuthToken 能成功执行 sql, 但已回滚: " +
                                                "%s",
                                        userDetails,
                                        authUser.getRawUserInfo(),
                                        MvcUtil.toJsonString(authToken));
                    log.error(msg, e);
                    throw new RegisterUserFailureException(ErrorCodeEnum.USER_REGISTER_OAUTH2_FAILURE,
                                                           userDetails.getUsername());
                }
            }

        }

    }

    /**
     * 添加到 第三方登录记录表
     * @param providerId    第三方服务商
     * @param authUser      authUser
     * @param userId        本地账户用户 Id
     * @param authToken     authToken
     */
    private void addConnectionData(String providerId, AuthUser authUser, String userId, AuthTokenPo authToken) {
        ConnectionData connectionData = JustAuthUtil.getConnectionData(providerId, authUser, userId, authToken);
        usersConnectionRepository.addConnection(connectionData);
    }

}
