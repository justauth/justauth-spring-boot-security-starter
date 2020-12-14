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
package demo.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import top.dcenter.ums.security.core.oauth.service.Auth2StateCoder;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

/**
 * 演示对 state 的加密解密, 进行传递参数
 * @author YongWu zheng
 * @version V2.0  Created by 2020/10/26 18:50
 */
@Component
public class Auth2StateCoderImpl implements Auth2StateCoder {

    @Override
    public String encode(@NonNull String state, @NonNull HttpServletRequest request) {
        final String referer = request.getHeader("Referer");
        if (StringUtils.hasText(referer)) {
            state = Base64Utils.encodeToUrlSafeString(referer.getBytes(StandardCharsets.UTF_8));
        }
        else {
            state = Base64Utils.encodeToUrlSafeString(state.getBytes(StandardCharsets.UTF_8));
        }
        // 其他混淆逻辑, 保证 state 安全
        return state;
    }

    @Override
    public String decode(@NonNull String encoderState) {
        // 其他解密混淆逻辑, 保证 state 安全
        return new String(Base64Utils.decodeFromUrlSafeString(encoderState), StandardCharsets.UTF_8);
    }
}
