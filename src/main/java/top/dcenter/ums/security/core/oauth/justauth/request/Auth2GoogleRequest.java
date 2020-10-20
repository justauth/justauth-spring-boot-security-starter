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

package top.dcenter.ums.security.core.oauth.justauth.request;

import lombok.Getter;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthDefaultSource;
import me.zhyd.oauth.enums.AuthResponseStatus;
import me.zhyd.oauth.log.Log;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.request.AuthGoogleRequest;
import me.zhyd.oauth.utils.AuthChecker;
import me.zhyd.oauth.utils.StringUtils;
import me.zhyd.oauth.utils.UuidUtils;
import top.dcenter.ums.security.core.oauth.entity.AuthTokenPo;
import top.dcenter.ums.security.core.oauth.justauth.Auth2RequestHolder;

import static top.dcenter.ums.security.core.oauth.justauth.request.Auth2DefaultRequest.*;

/**
 * {@link AuthGoogleRequest } 的扩展, 修改了 {@link #getRealState(String)} 与 {@link #login(AuthCallback)} 方法逻辑.
 * 修改 {@link #getAccessToken(AuthCallback)}, {@link #getUserInfo(AuthToken)} 等方法为 public 方法
 * @author YongWu zheng
 * @version V1.0  Created by 2020/10/7 16:18
 */
public class Auth2GoogleRequest extends AuthGoogleRequest implements Auth2DefaultRequest{

    @Getter
    private final String providerId;

    public Auth2GoogleRequest(AuthConfig config) {
        super(config);
        this.providerId = Auth2RequestHolder.getProviderId((AuthDefaultSource) this.source);
    }

    /**
     * @param config            config
     * @param authStateCache    stateCache
     */
    public Auth2GoogleRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, authStateCache);
        this.providerId = Auth2RequestHolder.getProviderId((AuthDefaultSource) this.source);
    }

    @Override
    protected String getRealState(String state) {
        if (StringUtils.isEmpty(state)) {
            state = UuidUtils.getUUID();
        }

        // 缓存 state
        authStateCache.cache(determineState(authStateCache, state, AuthDefaultSource.GOOGLE), state);
        return state;
    }

    /**
     * 统一的登录入口。当通过{@link AuthDefaultRequest#authorize(String)}授权成功后，会跳转到调用方的相关回调方法中
     * 方法的入参可以使用{@code AuthCallback}，{@code AuthCallback}类中封装好了OAuth2授权回调所需要的参数
     * @see AuthDefaultRequest#login(AuthCallback)
     * @param authCallback 用于接收回调参数的实体
     * @return AuthResponse
     */
    @SuppressWarnings("rawtypes")
    @Override
    public AuthResponse login(AuthCallback authCallback) {
        try {
            AuthChecker.checkCode(source, authCallback);
            if (!config.isIgnoreCheckState()) {
                AuthChecker.checkState(determineState(authStateCache, authCallback.getState(), AuthDefaultSource.GOOGLE),
                                       source, authStateCache);
            }

            AuthToken authToken = this.getAccessToken(authCallback);
            AuthUser user = this.getUserInfo(authToken);
            return AuthResponse.builder().code(AuthResponseStatus.SUCCESS.getCode()).data(user).build();
        } catch (Exception e) {
            Log.error("Failed to login with oauth authorization.", e);
            return Auth2DefaultRequest.responseError(e);
        }
    }

    @Override
    public AuthTokenPo refreshToken(AuthTokenPo authToken) {
        //noinspection rawtypes
        AuthResponse authResponse = super.refresh(authToken);
        return Auth2DefaultRequest.getAuthTokenPo(super.config.getHttpConfig().getTimeout(), authToken.getId(), authResponse);
    }

    @Override
    public AuthDefaultSource getAuthSource() {
        return (AuthDefaultSource) this.source;
    }

    @Override
    public AuthStateCache getAuthStateCache() {
        return this.authStateCache;
    }

    /**
     * 获取access token
     *
     * @param authCallback 授权成功后的回调参数
     * @return token
     * @see AuthDefaultRequest#authorize(String)
     */
    @Override
    public AuthToken getAccessToken(AuthCallback authCallback){
        return super.getAccessToken(authCallback);
    }

    /**
     * 使用token换取用户信息
     *
     * @param authToken token信息
     * @return 用户信息
     * @see AuthDefaultRequest#getAccessToken(AuthCallback)
     */
    @Override
    public AuthUser getUserInfo(AuthToken authToken) {
        return super.getUserInfo(authToken);
    }

}