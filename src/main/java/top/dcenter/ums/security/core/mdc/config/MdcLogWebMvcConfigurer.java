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
package top.dcenter.ums.security.core.mdc.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.dcenter.ums.security.core.mdc.interceptor.MdcLogInterceptor;
import top.dcenter.ums.security.core.mdc.properties.MdcProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于SLF4J MDC机制实现日志的链路追踪: MVC配置
 * @author YongWu zheng
 * @version V2.0  Created by 2020/10/31 18:15
 */
@Configuration
@ConditionalOnProperty(prefix = "ums.mdc", name = "enable", havingValue = "true")
@AutoConfigureAfter(value = {MdcPropertiesAutoConfiguration.class})
public class MdcLogWebMvcConfigurer implements WebMvcConfigurer {

    private final MdcProperties mdcProperties;
    private final List<String> excludeUrls;

    public MdcLogWebMvcConfigurer(MdcProperties mdcProperties) {
        this.mdcProperties = mdcProperties;
        this.excludeUrls = new ArrayList<>();
        final List<String> excludeUrls = mdcProperties.getExcludeUrls();
        if (null != excludeUrls) {
            this.excludeUrls.addAll(excludeUrls);
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mdcLogInterceptor())
                .addPathPatterns(this.mdcProperties.getIncludeUrls())
                .excludePathPatterns(this.excludeUrls);
    }

    @Bean
    public HandlerInterceptor mdcLogInterceptor() {
        return new MdcLogInterceptor();
    }
}
