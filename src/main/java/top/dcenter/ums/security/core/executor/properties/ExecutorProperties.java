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

package top.dcenter.ums.security.core.executor.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.dcenter.ums.security.core.oauth.enums.RejectedExecutionHandlerPolicy;
import top.dcenter.ums.security.core.oauth.job.RefreshTokenJob;
import top.dcenter.ums.security.core.oauth.provider.Auth2LoginAuthenticationProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 线程池属性
 * @author YongWu zheng
 * @version V2.0  Created by 2020/10/15 12:06
 */
@Getter
@ConfigurationProperties("ums.executor")
public class ExecutorProperties {

    /**
     * 启动第三方授权登录用户的 accessToken 的定时任务时的 Executor 属性,<br>
     * 注意: 需要根据实际生产环境进行优化
     */
    private final JobTaskScheduledExecutorProperties jobTaskScheduledExecutor = new JobTaskScheduledExecutorProperties();
    /**
     * 更新第三方授权登录用户的 accessToken 的执行逻辑, 向本地数据库 auth_token 表获取过期或在一定时间内过期的 token 记录, 用 refreshToken 向第三方服务商更新
     * accessToken 信息的 Executor 属性,<br>
     * 注意: 定时刷新 accessToken 的执行逻辑是多线程的, 需要根据实际生产环境进行优化
     */
    private final RefreshTokenExecutorProperties refreshToken = new RefreshTokenExecutorProperties();
    /**
     * 第三方授权登录时, 异步更新用户的第三方授权用户信息与 token 信息的 Executor 属性,<br>
     * 注意: 第三方授权登录时是异步更新第三方用户信息与 token 信息到本地数据库时使用此配置, 需要根据实际生产环境进行优化
     */
    private final UserConnectionUpdateExecutorProperties userConnectionUpdate = new UserConnectionUpdateExecutorProperties();

    @Getter
    @Setter
    public static class JobTaskScheduledExecutorProperties {

        /**
         * 线程池中空闲时保留的线程数, 默认: 0
         */
        private Integer corePoolSize = 0;
        /**
         * keep alive time, 默认: 10
         */
        private Integer keepAliveTime = 10;
        /**
         * keepAliveTime 时间单位, 默认: 毫秒
         */
        private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        /**
         * 线程池名称, 默认: accessTokenJob
         */
        private String poolName = "accessTokenJob";
        /**
         * 拒绝策略, 默认: ABORT
         */
        private RejectedExecutionHandlerPolicy rejectedExecutionHandlerPolicy = RejectedExecutionHandlerPolicy.ABORT;
        /**
         * 线程池关闭过程的超时时间, 默认: PT10S
         */
        private Duration executorShutdownTimeout = Duration.ofSeconds(10);
    }

    @Getter
    @Setter
    public static class RefreshTokenExecutorProperties {
        /**
         * 线程池中空闲时保留的线程数, 默认: 0
         */
        private Integer corePoolSize = 0;
        /**
         * 最大线程数, 默认: 本机核心数
         */
        private Integer maximumPoolSize = Runtime.getRuntime().availableProcessors();
        /**
         * keep alive time, 默认: 5
         */
        private Integer keepAliveTime = 5;
        /**
         * keepAliveTime 时间单位, 默认: 秒
         */
        private TimeUnit timeUnit = TimeUnit.SECONDS;
        /**
         * blocking queue capacity, 默认: maximumPoolSize * 2
         */
        private Integer blockingQueueCapacity = maximumPoolSize * 2;
        /**
         * 线程池名称, 默认: refreshToken
         */
        private String poolName = "refreshToken";
        /**
         * 拒绝策略, 默认: CALLER_RUNS<br>
         *     注意: 一般情况下不要更改默认设置, 没有实现 RefreshToken 逻辑被拒绝执行后的处理逻辑,
         *     除非自己实现{@link RefreshTokenJob#refreshTokenJob()} 对 RefreshToken 逻辑被拒绝执行后的处理逻辑.
         */
        private RejectedExecutionHandlerPolicy rejectedExecutionHandlerPolicy = RejectedExecutionHandlerPolicy.CALLER_RUNS;
        /**
         * 线程池关闭过程的超时时间, 默认: 10 秒
         */
        private Duration executorShutdownTimeout = Duration.ofSeconds(10);
    }

    @Getter
    @Setter
    public static class UserConnectionUpdateExecutorProperties {
        /**
         * 线程池中空闲时保留的线程数, 默认: 1
         */
        private Integer corePoolSize = 1;
        /**
         * 最大线程数, 默认: 本机核心数
         */
        private Integer maximumPoolSize = Runtime.getRuntime().availableProcessors();
        /**
         * keep alive time, 默认: 10
         */
        private Integer keepAliveTime = 10;
        /**
         * keepAliveTime 时间单位, 默认: 秒
         */
        private TimeUnit timeUnit = TimeUnit.SECONDS;
        /**
         * blocking queue capacity, 默认: maximumPoolSize * 2
         */
        private Integer blockingQueueCapacity = maximumPoolSize * 2;
        /**
         * 线程池名称, 默认: updateConnection
         */
        private String poolName = "updateConnection";
        /**
         * 拒绝策略, 默认: CALLER_RUNS<br>
         * 注意: 一般情况下不要更改默认设置, 除非自己实现{@link Auth2LoginAuthenticationProvider}更新逻辑;
         * 改成 ABORT 也支持, 默认实现 {@link Auth2LoginAuthenticationProvider} 是异步更新被拒绝执行后, 会执行同步更新.
         */
        private RejectedExecutionHandlerPolicy rejectedExecutionHandlerPolicy = RejectedExecutionHandlerPolicy.CALLER_RUNS;
        /**
         * 线程池关闭过程的超时时间, 默认: 10 秒
         */
        private Duration executorShutdownTimeout = Duration.ofSeconds(10);
    }
}