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

package top.dcenter.ums.security.core.oauth.job;

import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.exception.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import top.dcenter.ums.security.core.oauth.entity.AuthTokenPo;
import top.dcenter.ums.security.core.oauth.justauth.Auth2RequestHolder;
import top.dcenter.ums.security.core.oauth.justauth.request.Auth2DefaultRequest;
import top.dcenter.ums.security.core.oauth.properties.Auth2Properties;
import top.dcenter.ums.security.core.oauth.repository.UsersConnectionRepository;
import top.dcenter.ums.security.core.oauth.repository.UsersConnectionTokenRepository;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static top.dcenter.ums.security.core.oauth.enums.EnableRefresh.NO;

/**
 * 刷新 token 定时任务实现, 前提条件, {@link AuthTokenPo} auth_token 表的 id 为 Long 类型
 * @author YongWu zheng
 * @version V2.0  Created by 2020/10/14 14:03
 */
@Slf4j
public class RefreshTokenJobImpl implements RefreshTokenJob {

    /**
     * refresh token 定时任务锁的 redis key
     */
    public static final String REFRESH_TOKEN_JOB = "RefreshTokenJob:HashKey:lock";
    /**
     * refresh token 定时任务锁的 redis key 的过期时间, 单位: 小时
     */
    public static final Integer REFRESH_TOKEN_JOB_KEY_EXPIRED_IN = 6;

    private final UsersConnectionRepository usersConnectionRepository;
    private final UsersConnectionTokenRepository usersConnectionTokenRepository;
    private final Auth2Properties auth2Properties;
    private final ExecutorService refreshTokenTaskExecutor;
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    public RefreshTokenJobImpl(UsersConnectionRepository usersConnectionRepository,
                               @Autowired(required = false) UsersConnectionTokenRepository usersConnectionTokenRepository,
                               Auth2Properties auth2Properties,
                               @Qualifier("refreshTokenTaskExecutor") ExecutorService refreshTokenTaskExecutor) {
        Assert.notNull(refreshTokenTaskExecutor, "refreshTokenTaskExecutor cannot be null");
        Assert.notNull(usersConnectionRepository, "usersConnectionRepository cannot be null");
        Assert.notNull(usersConnectionTokenRepository, "usersConnectionTokenRepository cannot be null");
        Assert.notNull(auth2Properties, "auth2Properties cannot be null");

        this.refreshTokenTaskExecutor = refreshTokenTaskExecutor;
        this.usersConnectionRepository = usersConnectionRepository;
        this.usersConnectionTokenRepository = usersConnectionTokenRepository;
        this.auth2Properties = auth2Properties;
    }


    @Override
    public void refreshTokenJob() {
        // 不支持第三方 token 表(auth_token) 直接退出
        if (!auth2Properties.getEnableAuthTokenTable()) {
            return;
        }
        if (this.redisConnectionFactory != null)
        {
            // 分布式
            distributedRefreshToken();
        }
        else
        {
            // 单机
            refreshToken();
        }
    }

    /**
     * 分布式执行定时任务
     */
    private void distributedRefreshToken() {
        try (final RedisConnection connection = this.redisConnectionFactory.getConnection())
        {
            final Instant now = Instant.now();

            final byte[] key = REFRESH_TOKEN_JOB.getBytes(StandardCharsets.UTF_8.name());
            final long expiredIn = Duration.ofHours(REFRESH_TOKEN_JOB_KEY_EXPIRED_IN).getSeconds();
            // 设置过期时间
            connection.expireAt(key, now.plusSeconds(expiredIn).toEpochMilli());

            Long maxTokenId = usersConnectionTokenRepository.getMaxTokenId();
            Integer batchCount = auth2Properties.getBatchCount();
            long total = maxTokenId / batchCount + (maxTokenId % batchCount == 0 ? 0 : 1);
            log.info("分布式 refreshToken 定时刷新任务开始: 总批次={}, batchCount={}, maxTokenId={}",
                     total, batchCount, maxTokenId);
            for (int i = 0; i < total; i++)
            {
                try {
                    final byte[] field = Integer.toString(i).getBytes(StandardCharsets.UTF_8.name());
                    // 获取锁
                    final Boolean lock = connection.hSetNX(key, field, "0".getBytes(StandardCharsets.UTF_8.name()));
                    // 获取锁失败, 继续下一批次
                    if (lock == null || !lock)
                    {
                        log.info("分布式 refreshToken 定时刷新任务: 获取锁失败, 跳过第 {} 批次", i);
                        continue;
                    }
                    log.info("分布式 refreshToken 定时刷新任务: 获取锁成功, 执行第 {} 批次", i);
                    // 从数据库表 auth_token 获取符合条件的记录数; 从第三方刷新 token 信息, 并对 user_connection 与 auth_token 表进行更新
                    refresh(batchCount, i);
                }
                catch (UnsupportedEncodingException e) {
                    log.error(String.format("分布式 refreshToken 定时刷新任务 key(%d) 类型转换异常, error=%s", i, e.getMessage()), e);
                }
            }

            log.info("分布式 refreshToken 定时刷新任务结束: 总批次={}, batchCount={}, maxTokenId={}, 总耗时={} 毫秒",
                     total, batchCount, maxTokenId, Instant.now().toEpochMilli() - now.toEpochMilli());

        }
        catch (Exception e)
        {
            log.error(String.format("分布式 refreshToken 定时刷新任务异常, error=%s", e.getMessage()), e);
        }

    }

    /**
     * 单机执行定时任务
     */
    private void refreshToken() {
        try
        {
            long start = Instant.now().toEpochMilli();

            Long maxTokenId = usersConnectionTokenRepository.getMaxTokenId();
            Integer batchCount = auth2Properties.getBatchCount();
            long total = maxTokenId / batchCount + (maxTokenId % batchCount == 0 ? 0 : 1);

            log.info("refreshToken 定时刷新任务开始: 总批次={}, batchCount={}, maxTokenId={}",
                     total, batchCount, maxTokenId);
            for (int i = 0; i < total; i++)
            {
                log.info("refreshToken 定时刷新任务: 执行第 {} 批次", i);
                // 从数据库表 auth_token 获取符合条件的记录数; 从第三方刷新 token 信息, 并对 user_connection 与 auth_token 表进行更新
                refresh(batchCount, i);
            }

            log.info("refreshToken 定时刷新任务结束: 总批次={}, batchCount={}, maxTokenId={}, 总耗时={} 毫秒",
                     total, batchCount, maxTokenId, Instant.now().toEpochMilli() - start);
        }
        catch (Exception e)
        {
            log.error(String.format("单机 refreshToken 定时刷新任务异常, error=%s", e.getMessage()), e);
        }
    }

    /**
     * 从数据库表 auth_token 获取符合条件的记录数; 从第三方刷新 token 信息, 并对 user_connection 与 auth_token 表进行更新
     * @param batchCount    每次从数据库表 auth_token 获取的记录数
     * @param batch         迭代批次, 通过与 batchCount 来计算 tokenId 范围
     */
    private void refresh(Integer batchCount, int batch) {

        // 过期时间戳(获取小于此时间戳的记录)
        final long expiredTime = Instant.now().toEpochMilli() + Duration.ofHours(auth2Properties.getRemainingExpireIn()).toMillis();
        try {
            // 获取 token 记录
            List<AuthTokenPo> authTokenPoList =
                    usersConnectionTokenRepository.findAuthTokenByExpireTimeAndBetweenId(expiredTime,
                                                                                         1L + ((long) batch) * batchCount,
                                                                                         (batch + 1L) * batchCount);
            // 异步更新, 如果异步线程池处理过慢, refreshTokenTaskExecutor 的默认拒绝策略为 CallerRunsPolicy, 即改为同步更新
            authTokenPoList.forEach(
                    token ->
                    {
                        final Auth2DefaultRequest auth2DefaultRequest = Auth2RequestHolder.getAuth2DefaultRequest(token.getProviderId());
                        refreshTokenTaskExecutor.execute(() -> getTokenAndUpdateAuthTokenPo(token, auth2DefaultRequest));
                    }
            );
        }
        catch (Exception e) {
            log.error(String.format("refreshToken 定时刷新任务从 auth_token 获取的记录数出现异常: 第 %d 批次, batchCount=%d, error=%s",
                                    batch, batchCount, e.getMessage()), e);
        }
    }

    /**
     * 从第三方刷新 token 信息, 并对 user_connection 与 auth_token 表进行更新
     * @param token                 {@link AuthTokenPo}
     * @param auth2DefaultRequest   {@link Auth2DefaultRequest}
     */
    private void getTokenAndUpdateAuthTokenPo(@NonNull AuthTokenPo token,
                                         @Nullable Auth2DefaultRequest auth2DefaultRequest) {
        if (auth2DefaultRequest != null)
        {
            try {
                AuthTokenPo authTokenPo;
                try {
                    // 从第三方刷新 accessToken
                    authTokenPo = auth2DefaultRequest.refreshToken(token);
                }
                catch (Exception e) {
                    String msg;

                    if (e instanceof AuthException)
                    {
                        msg = String.format("RefreshToken 第三方 %s 不支持: tokenId=%s",
                                            token.getProviderId(), token.getId());
                        log.info(msg);
                        authTokenPo = token;
                        authTokenPo.setEnableRefresh(NO);
                        // 更新为第三方不支持 refresh token
                        usersConnectionTokenRepository.updateEnableRefreshByTokenId(NO, token.getId());
                    }
                    else
                    {
                        msg = String.format("RefreshToken 失败: tokenId=%s, error=%s",
                                            token.getId(), e.getMessage());
                        log.error(msg, e);
                    }
                    return;
                }

                // 根据 authTokenPo 对 user_connection 与 auth_token 表进行更新
                updateAuthTokenPo(authTokenPo);

            }
            catch (Exception e) {
                String msg = String.format("RefreshToken 失败: tokenId=%s, error=%s",
                                           token.getId(), e.getMessage());
                log.error(msg, e);
            }
        }
        else
        {
            log.info("RefreshToken 不支持: providerId={}, ", token.getProviderId());
        }
    }

    /**
     * 根据 token 对 user_connection 与 auth_token 表进行更新
     * @param token         {@link AuthTokenPo}
     * @throws Exception    更新 user_connection 或 auth_token 出现错误.
     */
    @Transactional(rollbackFor = {Exception.class}, propagation = Propagation.REQUIRED)
    public void updateAuthTokenPo(@NonNull AuthTokenPo token) throws Exception {
        usersConnectionTokenRepository.updateAuthToken(token);
        usersConnectionRepository.updateConnectionByTokenId(token);
    }

}