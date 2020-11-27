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
package demo.handler;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.dcenter.ums.security.core.oauth.userdetails.TemporaryUser;
import top.dcenter.ums.security.core.vo.ResponseResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static top.dcenter.ums.security.core.oauth.util.MvcUtil.isAjaxOrJson;
import static top.dcenter.ums.security.core.oauth.util.MvcUtil.responseWithJson;
import static top.dcenter.ums.security.core.oauth.util.MvcUtil.toJsonString;

/**
 * 演示 signUpUrl 设置为 null 时的一种处理方式
 * @author YongWu zheng
 * @version V2.0  Created by 2020/10/30 10:19
 */
@Component
public class DemoSignUpUrlAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication authentication)
            throws IOException {


        // start: 判断是否为临时用户, 进行相关逻辑的处理
        final Object principal = authentication.getPrincipal();
        if (principal instanceof TemporaryUser)
        {
            TemporaryUser temporaryUser = ((TemporaryUser) principal);
            // 自己的处理逻辑, 如返回 json 数据
            // ...
            return;
        }
        // end: 判断是否为临时用户, 进行相关逻辑的处理

        String targetUrl = null;
        if (isAlwaysUseDefaultTargetUrl()) {
            targetUrl = getDefaultTargetUrl();
        }
        else {
            String targetUrlParameter = getTargetUrlParameter();
            if (targetUrlParameter != null && StringUtils.hasText(request.getParameter(targetUrlParameter))) {
                targetUrl = targetUrlParameter;
            }
            else {
                // Use the DefaultSavedRequest URL
                SavedRequest savedRequest = requestCache.getRequest(request, response);
                if (savedRequest != null) {
                    targetUrl = savedRequest.getRedirectUrl();
                }
            }
        }

        clearAuthenticationAttributes(request);

        if (!StringUtils.hasText(targetUrl)) {
            targetUrl = getDefaultTargetUrl();
        }

        logger.debug("Redirecting to DefaultSavedRequest Url: " + targetUrl);

        if (isAjaxOrJson(request)) {
            responseWithJson(response, HttpStatus.OK.value(), toJsonString(ResponseResult.success("url", targetUrl)));
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @Override
    public void setRequestCache(RequestCache requestCache) {
        this.requestCache = requestCache;
    }
}
