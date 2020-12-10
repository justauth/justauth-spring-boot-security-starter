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

package top.dcenter.ums.security.core.oauth.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * OAuth2 基本属性
 * @author YongWu zheng
 * @version V1.0  Created by 2020/5/17 14:08
 */
@Getter
@Setter
public class BaseAuth2Properties {

    private String clientId;
    private String clientSecret;
    /**
     * 使用 Coding 登录时，需要传该值。
     * <p>
     * 团队域名前缀，比如以“ https://justauth.coding.net/ ”为例，{@code codingGroupName} = justauth
     *
     * @since 1.15.5
     */
    private String codingGroupName;
    /**
     * 支付宝公钥：当选择支付宝登录时，该值可用
     * 对应“RSA2(SHA256)密钥”中的“支付宝公钥”
     */
    private String alipayPublicKey;
    /**
     * 是否需要申请 unionId，默认: false. 目前只针对qq登录
     * 注：qq授权登录时，获取 unionId 需要单独发送邮件申请权限。如果个人开发者账号中申请了该权限，可以将该值置为true，在获取openId时就会同步获取unionId
     * 参考链接：http://wiki.connect.qq.com/unionid%E4%BB%8B%E7%BB%8D
     * <p>
     * 1.7.1版本新增参数
     */
    private Boolean unionId = false;
    /**
     * Stack Overflow Key
     * <p>
     *
     * @since 1.9.0
     */
    private String stackOverflowKey;
    /**
     * 企业微信，授权方的网页应用ID
     *
     * @since 1.10.0
     */
    private String agentId;
}