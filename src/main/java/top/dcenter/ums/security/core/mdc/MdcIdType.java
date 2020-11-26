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
package top.dcenter.ums.security.core.mdc;

import me.zhyd.oauth.utils.UuidUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * 基于 SLF4J MDC 机制实现日志链路追踪 id 的类型枚举
 * @author YongWu zheng
 * @version V2.0  Created by 2020.11.26 20:25
 */
public enum MdcIdType implements MdcIdGenerator {
    /**
     * UUID
     */
    UUID {
        @Override
        public String getMdcId() {
            return UuidUtils.getUUID();
        }
    },
    /**
     * 线程 id
     */
    THREAD_ID {
        @Override
        public String getMdcId() {
            return Thread.currentThread().getId() + "";
        }
    },
    /**
     * session id
     */
    SESSION_ID {
        @Override
        public String getMdcId() {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) {
                return UuidUtils.getUUID();
            }
            return requestAttributes.getSessionId();
        }
    },
    /**
     * 自定义 id
     */
    CUSTOMIZE_ID

}
