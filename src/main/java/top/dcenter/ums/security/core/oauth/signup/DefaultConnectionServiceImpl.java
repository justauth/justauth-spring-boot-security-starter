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

package top.dcenter.ums.security.core.oauth.signup;

import com.xkcoding.http.config.HttpConfig;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.dcenter.ums.security.core.oauth.entity.AuthTokenPo;
import top.dcenter.ums.security.core.oauth.entity.ConnectionData;
import top.dcenter.ums.security.core.oauth.enums.ErrorCodeEnum;
import top.dcenter.ums.security.core.oauth.exception.RegisterUserFailureException;
import top.dcenter.ums.security.core.oauth.justauth.request.Auth2DefaultRequest;
import top.dcenter.ums.security.core.oauth.justauth.util.JustAuthUtil;
import top.dcenter.ums.security.core.oauth.properties.Auth2Properties;
import top.dcenter.ums.security.core.oauth.repository.UsersConnectionRepository;
import top.dcenter.ums.security.core.oauth.repository.UsersConnectionTokenRepository;
import top.dcenter.ums.security.core.oauth.repository.exception.UpdateConnectionException;
import top.dcenter.ums.security.core.oauth.service.Auth2StateCoder;
import top.dcenter.ums.security.core.oauth.service.UmsUserDetailsService;

import java.util.List;

import static top.dcenter.ums.security.core.oauth.util.MvcUtil.toJsonString;

/**
 * 默认的第三方授权登录时自动注册处理器。<br>
 * {@link #signUp(AuthUser, String, String)} 功能：第三方登录自动注册时, 根据 第三方的 authUser 注册为本地账户的用户,
 * 用户名的生成规则由 {@link UmsUserDetailsService#generateUsernames(AuthUser)} 定义, 默认规则为:
 * username 或 username + "_" + providerId 或 username + "_" + providerId + "_" + providerUserId.
 * 如需自定义用户名生成逻辑, 重新实现 {@link UmsUserDetailsService#generateUsernames(AuthUser)} 方法即可
 * @author YongWu zheng
 * @version V2.0  Created by 2020/5/14 22:32
 * @see ConnectionService
 */
@Slf4j
public class DefaultConnectionServiceImpl implements ConnectionService {
    /**
     * {@link HttpConfig#getTimeout()}, 单位毫秒,
     * 返回用户设置的超时时间{@link Auth2Properties.HttpConfigProperties#getTimeout()}，单位毫秒.
     */
    private final Integer timeout;

    private final UmsUserDetailsService userDetailsService;
    private final String defaultAuthorities;
    private final UsersConnectionRepository usersConnectionRepository;
    private final UsersConnectionTokenRepository usersConnectionTokenRepository;
    private final Auth2StateCoder auth2StateCoder;

    public DefaultConnectionServiceImpl(UmsUserDetailsService userDetailsService,
                                        Auth2Properties auth2Properties,
                                        UsersConnectionRepository usersConnectionRepository,
                                        UsersConnectionTokenRepository usersConnectionTokenRepository,
                                        Auth2StateCoder auth2StateCoder) {
        this.userDetailsService = userDetailsService;
        this.defaultAuthorities = auth2Properties.getDefaultAuthorities();
        this.usersConnectionRepository = usersConnectionRepository;
        this.usersConnectionTokenRepository = usersConnectionTokenRepository;
        this.timeout = auth2Properties.getProxy().getHttpConfig().getTimeout();
        this.auth2StateCoder = auth2StateCoder;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class}, propagation = Propagation.REQUIRES_NEW)
    public UserDetails signUp(@NonNull AuthUser authUser, @NonNull String providerId, @NonNull String encodeState) throws RegisterUserFailureException {
        // 这里为第三方登录自动注册时调用，所以这里不需要实现对用户信息的注册，可以在用户登录完成后提示用户修改用户信息。
        String username = authUser.getUsername();
        String[] usernames = userDetailsService.generateUsernames(authUser);

        try {
            // 重名检查
            username = null;
            final List<Boolean> existedByUserIds = userDetailsService.existedByUsernames(usernames);
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
            UserDetails userDetails = userDetailsService.registerUser(authUser, username, defaultAuthorities, decodeState);
            // 第三方授权登录信息绑定到本地账号, 且添加第三方授权登录信息到 user_connection 与 auth_token
            registerConnection(providerId, authUser, userDetails);

            return userDetails;
        }
        catch (Exception e) {
            log.error(String.format("OAuth2自动注册失败: error=%s, username=%s, authUser=%s",
                                    e.getMessage(), username, toJsonString(authUser)), e);
            throw new RegisterUserFailureException(ErrorCodeEnum.USER_REGISTER_FAILURE, username);
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class}, propagation = Propagation.REQUIRED)
    public void updateUserConnection(@NonNull AuthUser authUser, @NonNull ConnectionData data) throws UpdateConnectionException {
        ConnectionData connectionData = null;
        try
        {
            // 获取 AuthTokenPo
            AuthToken token = authUser.getToken();
            AuthTokenPo authToken = JustAuthUtil.getAuthTokenPo(token, data.getProviderId(), this.timeout);
            authToken.setId(data.getTokenId());
            // 有效期转时间戳
            Auth2DefaultRequest.expireIn2Timestamp(this.timeout, token.getExpireIn(), authToken);

            // 获取最新的 ConnectionData
            connectionData = JustAuthUtil.getConnectionData(data.getProviderId(), authUser, data.getUserId(), authToken);
            connectionData.setUserId(data.getUserId());
            connectionData.setTokenId(data.getTokenId());

            // 更新 connectionData
            usersConnectionRepository.updateConnection(connectionData);
            // 更新 AuthTokenPo
            usersConnectionTokenRepository.updateAuthToken(authToken);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            throw new UpdateConnectionException(ErrorCodeEnum.UPDATE_CONNECTION_DATA_FAILURE, connectionData, e);
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class}, propagation = Propagation.REQUIRED)
    public void binding(@NonNull UserDetails principal, @NonNull AuthUser authUser, @NonNull String providerId) {
        // 第三方授权登录信息绑定到本地账号, 且添加第三方授权登录信息到 user_connection 与 auth_token
        registerConnection(providerId, authUser, principal);
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
                                        userDetails, toJsonString(authUser));
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
                                        toJsonString(authToken));
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