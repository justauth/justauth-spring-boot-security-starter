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

import me.zhyd.oauth.config.AuthDefaultSource;
import org.springframework.stereotype.Component;
import top.dcenter.ums.security.core.oauth.justauth.request.AuthCustomizeRequest;
import top.dcenter.ums.security.core.oauth.justauth.source.AuthCustomizeSource;
import top.dcenter.ums.security.core.oauth.justauth.source.AuthGitlabPrivateSource;

/**
 * 演示如何自定义 OAuth2 Login.<br>
 * 注意:<br>
 * 1. 必须同时实现 {@link AuthCustomizeRequest}, 示例: {@link AuthCustomizeGiteeRequest}.<br>
 * 2. 里面的除了{@link #getCustomizeRequestClass()}方法, 其他方法 COPY 至 {@link AuthDefaultSource#GITEE} .<br>
 * 3. {@link AuthGitlabPrivateSource} 的自定义可参考此类.
 * @author YongWu zheng
 * @version V2.0  Created by 2020.11.29 16:31
 */
@Component
public class AuthCustomizeGiteeSource extends AuthCustomizeSource {

    @Override
    public String authorize() {
        return "https://gitee.com/oauth/authorize";
    }

    @Override
    public String accessToken() {
        return "https://gitee.com/oauth/token";
    }

    @Override
    public String userInfo() {
        return "https://gitee.com/api/v5/user";
    }

    @Override
    public Class<? extends AuthCustomizeRequest> getCustomizeRequestClass() {
        return AuthCustomizeGiteeRequest.class;
    }
}
