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
package top.dcenter.ums.security.core.mdc.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.dcenter.ums.security.core.mdc.MdcIdType;
import top.dcenter.ums.security.core.mdc.MdcIdGenerator;

import java.util.HashSet;
import java.util.Set;

/**
 * 基于 SLF4J MDC 机制日志的链路追踪: 日志属性
 * @author YongWu zheng
 * @version V2.0  Created by 2020/10/31 19:43
 */
@ConfigurationProperties("ums.mdc")
public class MdcProperties {

    public MdcProperties() {
        this.includeUrls = new HashSet<>();
        includeUrls.add("/**");
    }

    /**
     * 是否支持基于 SLF4J MDC 机制日志的链路追踪, 默认: true
     */
    @Setter
    @Getter
    private Boolean enable = true;

    /**
     * 基于 SLF4J MDC 机制实现日志链路追踪 id 的类型, 默认为 uuid.<br>
     * 当需要自定义 id 时, type = {@link MdcIdType#CUSTOMIZE_ID}, 再实现 {@link MdcIdGenerator#getMdcId()} 方法, 注入 IOC 容器即可.
     */
    @Setter
    @Getter
    private MdcIdType type = MdcIdType.UUID;

    /**
     * 需要添加 MDC 日志的链路追踪的 url, 默认: /**, 并在日志文件的 pattern 中添加 %X{MDC_TRACE_ID}
     */
    @Setter
    @Getter
    private Set<String> includeUrls;
    /**
     * 不需要 MDC 日志的链路追踪的 url, 如: 静态路径
     */
    @Setter
    @Getter
    private Set<String> excludeUrls = new HashSet<>();
}
