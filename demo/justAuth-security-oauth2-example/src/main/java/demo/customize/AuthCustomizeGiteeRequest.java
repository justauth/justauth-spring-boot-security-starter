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
package demo.customize;

import com.alibaba.fastjson.JSONObject;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.enums.AuthUserGender;
import me.zhyd.oauth.enums.scope.AuthGiteeScope;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.utils.AuthScopeUtils;
import me.zhyd.oauth.utils.UrlBuilder;
import top.dcenter.ums.security.core.oauth.justauth.request.AuthCustomizeRequest;
import top.dcenter.ums.security.core.oauth.justauth.source.AuthCustomizeSource;

/**
 * 演示如何自定义 OAuth2 Login.<br>
 * 注意:<br>
 * 1. 必须同时实现 {@link AuthCustomizeSource}, 示例: {@link AuthCustomizeGiteeSource}.<br>
 * 2. 里面的实现逻辑 COPY 至 {@link AuthGiteeRequest} .
 * @author YongWu zheng
 * @version V2.0  Created by 2020.11.29 16:28
 */
public class AuthCustomizeGiteeRequest extends AuthCustomizeRequest {

    public AuthCustomizeGiteeRequest(AuthConfig config) {
        super(config, new AuthCustomizeGiteeSource());
    }

    public AuthCustomizeGiteeRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, new AuthCustomizeGiteeSource(), authStateCache);
    }

    @Override
    protected AuthToken getAccessToken(AuthCallback authCallback) {
        String response = doPostAuthorizationCode(authCallback.getCode());
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        this.checkResponse(accessTokenObject);
        return AuthToken.builder()
                        .accessToken(accessTokenObject.getString("access_token"))
                        .refreshToken(accessTokenObject.getString("refresh_token"))
                        .scope(accessTokenObject.getString("scope"))
                        .tokenType(accessTokenObject.getString("token_type"))
                        .expireIn(accessTokenObject.getIntValue("expires_in"))
                        .build();
    }

    @Override
    protected AuthUser getUserInfo(AuthToken authToken) {
        String userInfo = doGetUserInfo(authToken);
        JSONObject object = JSONObject.parseObject(userInfo);
        this.checkResponse(object);
        return AuthUser.builder()
                       .rawUserInfo(object)
                       .uuid(object.getString("id"))
                       .username(object.getString("login"))
                       .avatar(object.getString("avatar_url"))
                       .blog(object.getString("blog"))
                       .nickname(object.getString("name"))
                       .company(object.getString("company"))
                       .location(object.getString("address"))
                       .email(object.getString("email"))
                       .remark(object.getString("bio"))
                       .gender(AuthUserGender.UNKNOWN)
                       .token(authToken)
                       .source(source.toString())
                       .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("error")) {
            throw new AuthException(object.getString("error_description"));
        }
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return UrlBuilder.fromBaseUrl(super.authorize(state))
                         .queryParam("scope", this.getScopes(" ", true, AuthScopeUtils.getDefaultScopes(AuthGiteeScope.values())))
                         .build();
    }
}
