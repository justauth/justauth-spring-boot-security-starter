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
package top.dcenter.ums.security.core.tasks.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.dcenter.ums.security.core.executor.config.ExecutorAutoConfiguration;
import top.dcenter.ums.security.core.oauth.config.Auth2AutoConfiguration;
import top.dcenter.ums.security.core.tasks.handler.RefreshAccessTokenJobHandler;

/**
 * 简单的任务定时任务配置
 * @author YongWu zheng
 * @version V2.0  Created by 2020.11.26 17:16
 */
@Configuration
@AutoConfigureAfter({ExecutorAutoConfiguration.class, Auth2AutoConfiguration.class})
public class TasksAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "ums.oauth", name = "enable-refresh-token-job", havingValue = "true")
    public RefreshAccessTokenJobHandler refreshAccessTokenJobHandler() {
        return new RefreshAccessTokenJobHandler();
    }
}
