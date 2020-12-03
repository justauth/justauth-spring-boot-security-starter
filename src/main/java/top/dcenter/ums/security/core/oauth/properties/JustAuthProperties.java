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
import me.zhyd.oauth.cache.AuthCacheConfig;
import me.zhyd.oauth.model.AuthCallback;
import top.dcenter.ums.security.core.oauth.justauth.enums.StateCacheType;

import java.time.Duration;
import java.util.List;

/**
 * JustAuth 配置
 * @author YongWu zheng
 * @version V1.0  Created by 2020/10/6 19:58
 */
@Getter
@Setter
public class JustAuthProperties {

    /**
     * 忽略校验 {@code state} 参数，默认不开启。当 {@code ignoreCheckState} 为 {@code true} 时，
     * {@link me.zhyd.oauth.request.AuthDefaultRequest#login(AuthCallback)} 将不会校验 {@code state} 的合法性。
     * <p>
     * 使用场景：当且仅当使用自实现 {@code state} 校验逻辑时开启
     * <p>
     * 以下场景使用方案仅作参考：
     * 1. 授权、登录为同端，并且全部使用 JustAuth 实现时，该值建议设为 {@code false};
     * 2. 授权和登录为不同端实现时，比如前端页面拼装 {@code authorizeUrl}，并且前端自行对{@code state}进行校验，
     * 后端只负责使用{@code code}获取用户信息时，该值建议设为 {@code true};
     *
     * <strong>如非特殊需要，不建议开启这个配置</strong>
     * <p>
     * 该方案主要为了解决以下类似场景的问题：
     *
     * @see <a href="https://github.com/justauth/JustAuth/issues/83">https://github.com/justauth/JustAuth/issues/83</a>
     * @since 1.15.6
     */
    private Boolean ignoreCheckState = false;

    /**
     * 默认 state 缓存过期时间：3分钟(PT180S)
     * 鉴于授权过程中，根据个人的操作习惯，或者授权平台的不同（google等），每个授权流程的耗时也有差异，不过单个授权流程一般不会太长
     * 本缓存工具默认的过期时间设置为3分钟，即程序默认认为3分钟内的授权有效，超过3分钟则默认失效，失效后删除
     */
    private Duration timeout = Duration.ofMillis(AuthCacheConfig.timeout);

    /**
     * JustAuth state 缓存类型, 默认 session
     */
    private StateCacheType cacheType = StateCacheType.SESSION;

    /**
     * JustAuth state 缓存 key 前缀
     */
    private String cacheKeyPrefix = "JUST_AUTH:";

    /**
     * 支持自定义授权平台的 scope 内容, 格式为: providerId:scope, 例如: [qq:write, qq:read, gitee:email, github:read]
     * <pre>
     * ums:
     *   oauth:
     *     just-auth:
     *       scopes:
     *         - qq:write
     *         - qq:read
     *         - gitee:email
     *         - github:read
     * </pre>
     * @since 1.15.7
     */
    private List<String> scopes;

}