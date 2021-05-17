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
package top.dcenter.ums.security.core.oauth.oneclicklogin;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.ServletWebRequest;
import top.dcenter.ums.security.core.oauth.consts.SecurityConstants;
import top.dcenter.ums.security.core.oauth.enums.ErrorCodeEnum;
import top.dcenter.ums.security.core.oauth.exception.Auth2Exception;
import top.dcenter.ums.security.core.oauth.oneclicklogin.service.OneClickLoginService;
import top.dcenter.ums.security.core.oauth.properties.OneClickLoginProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

/**
 * 一键登录配置过滤器
 * @author YongWu zheng
 * @weixin z56133
 * @since 2021.5.13 15:15
 */
public class OneClickLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    // ~ Static fields/initializers
    // =====================================================================================

    private final String tokenParamName;
    /**
     * 其他请求参数名称列表(包括请求头名称)
     */
    private final List<String> otherParamNames;
    private final OneClickLoginService oneClickLoginService;
    private boolean postOnly = true;

    // ~ Constructors
    // ===================================================================================================

    public OneClickLoginAuthenticationFilter(@NonNull OneClickLoginService oneClickLoginService,
                                             @NonNull OneClickLoginProperties oneClickLoginProperties,
                                             @Nullable AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        super(new AntPathRequestMatcher(oneClickLoginProperties.getLoginProcessingUrl(), SecurityConstants.POST_METHOD));
        this.oneClickLoginService = oneClickLoginService;
        this.tokenParamName = oneClickLoginProperties.getTokenParamName();
        this.otherParamNames = oneClickLoginProperties.getOtherParamNames();
        if (authenticationDetailsSource != null) {
            setAuthenticationDetailsSource(authenticationDetailsSource);
        }
    }

    // ~ Methods
    // ========================================================================================================

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        if (postOnly && !request.getMethod().equals(SecurityConstants.POST_METHOD)) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }

        String accessToken = obtainAccessToken(request);
        if (StringUtils.isEmpty(accessToken)) {
            throw new Auth2Exception(ErrorCodeEnum.ACCESS_TOKEN_NOT_EMPTY, this.tokenParamName);
        }

        accessToken = accessToken.trim();
        Map<String, String> otherParamMap = getOtherParamMap(this.otherParamNames, request);
        String mobile = this.oneClickLoginService.callback(accessToken, otherParamMap);

        OneClickLoginAuthenticationToken authRequest =
                new OneClickLoginAuthenticationToken(mobile, otherParamMap);

        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);
        // 一键登录: 用户已注册则登录, 未注册则自动注册用户且返回登录状态
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    /**
     * 从 request 中获取 otherParamNames 的 paramValue
     * @param otherParamNames   参数名称列表
     * @param request           request
     * @return map(paramName, paramValue)
     */
    @Nullable
    protected Map<String, String> getOtherParamMap(@NonNull List<String> otherParamNames,
                                                   @NonNull HttpServletRequest request) {
        if (otherParamNames.isEmpty()) {
            return null;
        }
        // map(paramName, paramValue)
        final Map<String, String> otherMap = new HashMap<>(otherParamNames.size());
        otherParamNames.forEach(name ->{
            try {
                String value = ServletRequestUtils.getStringParameter(request, name);
                otherMap.put(name, value);
            }
            catch (ServletRequestBindingException e) {
                String headerValue = request.getHeader(name);
                if (nonNull(headerValue)) {
                    otherMap.put(name, headerValue);
                }
            }
        });

        return otherMap;
    }


    /**
     * 获取 access token;
     *
     * @param request so that request attributes can be retrieved
     *
     * @return access token
     */
    protected String obtainAccessToken(HttpServletRequest request) {
        ServletWebRequest servletWebRequest = new ServletWebRequest(request, null);
        return servletWebRequest.getParameter(tokenParamName);
    }

    /**
     * Provided so that subclasses may configure what is put into the auth
     * request's details property.
     *
     * @param request that an auth request is being created for
     * @param authRequest the auth request object that should have its details
     * set
     */
    protected void setDetails(HttpServletRequest request,
                              OneClickLoginAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }

    /**
     * Defines whether only HTTP POST requests will be allowed by this filter. If set to
     * true, and an auth request is received which is not a POST request, an
     * exception will be raised immediately and auth will not be attempted. The
     * <tt>unsuccessfulAuthentication()</tt> method will be called as if handling a failed
     * auth.
     * <p>
     * Defaults to <tt>true</tt> but may be overridden by subclasses.
     */
    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

}
