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

package top.dcenter.ums.security.core.oauth.justauth.cache;

import me.zhyd.oauth.cache.AuthCacheConfig;
import me.zhyd.oauth.config.AuthDefaultSource;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import top.dcenter.ums.security.core.oauth.justauth.enums.CacheKeyStrategy;
import top.dcenter.ums.security.core.oauth.properties.Auth2Properties;
import top.dcenter.ums.security.core.oauth.properties.JustAuthProperties;

import java.time.Instant;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

/**
 * auth state session cache, 根据 session 的缓存模式是否适用分布式来决定是否适用单机与分布式<br>
 *     1. 传入的 key 必须为 {@link AuthDefaultSource} 的 <code>name()</code>. 这样相同 session
 *     与相同的第三方 {@link AuthDefaultSource} 的 <code>name()</code> 的 cache key 永远相同 <br>
 *     2. 默认缓存时间为 {@link AuthCacheConfig#timeout}. <br>
 *     3. 清除缓存时间点: 获取缓存时(<code>get(key)</code>)会判断是否过期, 过期则删除, 调用 <code>containsKey(key)</code> 时会走 <code>get(key)
 *     </code> 流程. <br>
 *     4. 相同 session 与 相同的第三方 {@link AuthDefaultSource} 的 <code>name()</code>, <code>cache(key, value)</code> 会覆盖上一次的 value; <br>
 *
 * @author YongWu zheng
 * @version V1.0  Created by 2020/10/6 15:59
 */
public class AuthStateSessionCache implements Auth2StateCache {

    /**
     * value 与 timeout 的分隔符
     */
    private static final String DELIMITER = "_";

    private final JustAuthProperties justAuthProperties;

    public AuthStateSessionCache(Auth2Properties auth2Properties) {
        this.justAuthProperties = auth2Properties.getJustAuth();
    }

    @Override
    public void cache(String key, String value) {
        this.cache(key, value, justAuthProperties.getTimeout().toMillis());
    }

    @Override
    public void cache(String key, String value, long timeout) {
        long epochMilli = Instant.now().plusMillis(timeout).toEpochMilli();
        RequestContextHolder.currentRequestAttributes().setAttribute(justAuthProperties.getCacheKeyPrefix() + key,
                                                                     value + DELIMITER + epochMilli, SCOPE_SESSION);
    }

    @Override
    public String get(String key) {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        String sessionKey = justAuthProperties.getCacheKeyPrefix() + key;

        String result = (String) requestAttributes.getAttribute(sessionKey, SCOPE_SESSION);

        if (!StringUtils.hasText(result))
        {
            return null;
        }

        int index = result.lastIndexOf(DELIMITER);

        long timeout = Long.parseLong(result.substring(index + 1));
        if (Instant.now().toEpochMilli() > timeout)
        {
            requestAttributes.removeAttribute(sessionKey, SCOPE_SESSION);
            return null;
        }
        return result.substring(0, index);
    }

    /**
     * 移除缓存
     * @param key   state cache key
     */
    public void remove(String key) {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        String sessionKey = justAuthProperties.getCacheKeyPrefix() + key;
        requestAttributes.removeAttribute(sessionKey, SCOPE_SESSION);
    }

    @Override
    public boolean containsKey(String key) {
        return StringUtils.hasText(this.get(key));
    }

    @Override
    public CacheKeyStrategy getCacheKeyStrategy() {
        return CacheKeyStrategy.PROVIDER_ID;
    }
}