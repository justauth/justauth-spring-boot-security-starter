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

package top.dcenter.ums.security.core.oauth.entity;

import lombok.Getter;
import lombok.Setter;
import me.zhyd.oauth.model.AuthToken;
import top.dcenter.ums.security.core.oauth.enums.EnableRefresh;

/**
 * {@link AuthToken} 持久化 PO
 * @author YongWu zheng
 * @version V2.0  Created by 2020/10/10 14:10
 */
@SuppressWarnings("jol")
@Setter
@Getter
public class AuthTokenPo extends AuthToken {
    private static final long serialVersionUID = -295423281641462728L;

    /**
     * tokenId
     */
    private Long id;
    /**
     * 第三方服务商(如: qq,github)
     */
    private String providerId;

    /**
     * 过期日期, 基于 1970-01-01T00:00:00Z, 无过期时间默认为 -1
     */
    private Long expireTime;
    /**
     * 是否支持 refreshToken, 默认: {@code EnableRefresh.YES}. 数据库存储 int 值:1 表示支持, 0 表示不支持
     */
    private EnableRefresh enableRefresh = EnableRefresh.YES;
}