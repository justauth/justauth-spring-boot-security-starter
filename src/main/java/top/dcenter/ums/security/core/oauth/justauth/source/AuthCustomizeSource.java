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
package top.dcenter.ums.security.core.oauth.justauth.source;

import me.zhyd.oauth.config.AuthSource;
import top.dcenter.ums.security.core.oauth.justauth.request.AuthCustomizeRequest;

/**
 * 抽象类, 实现此自定义的 {@link AuthCustomizeSource} 且注入 ioc 容器的同时, 必须实现  {@link AuthCustomizeRequest} ,
 * 会自动集成进 OAuth2 Login 逻辑流程中,
 * 只需要像 JustAuth 默认实现的第三方登录一样, 配置相应的属性(ums.oauth.customize.[clientId|clientSecret]等属性)即可.
 * @author YongWu zheng
 * @version V2.0  Created by 2020.11.29 13:14
 */
@SuppressWarnings("AlibabaAbstractClassShouldStartWithAbstractNaming")
public abstract class AuthCustomizeSource implements AuthSource {

    /**
     * 获取自定义 {@link AuthSource} 的字符串名字
     *
     * @return name
     */
    @Override
    public final String getName() {
        return "CUSTOMIZE";
    }

    /**
     * 获取 {@link AuthCustomizeRequest} 的实现类的 Class.
     * @return 返回 {@link AuthCustomizeRequest} 的实现类的 Class
     */
    public abstract Class<? extends AuthCustomizeRequest> getCustomizeRequestClass();

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AuthCustomizeSource)) {
            return false;
        }
        return this.getName().equals(((AuthCustomizeSource) obj).getName());
    }

}
