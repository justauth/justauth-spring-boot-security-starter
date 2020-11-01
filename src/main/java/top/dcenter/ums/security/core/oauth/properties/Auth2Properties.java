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

import com.xkcoding.http.config.HttpConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import top.dcenter.ums.security.core.oauth.filter.login.Auth2LoginAuthenticationFilter;
import top.dcenter.ums.security.core.oauth.userdetails.TemporaryUser;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;

/**
 * 第三方授权登录属性
 * @author YongWu zheng
 * @version V1.0  Created by 2020/10/6 21:01
 */
@SuppressWarnings({"jol"})
@Getter
@ConfigurationProperties("ums.oauth")
public class Auth2Properties {

    // =================== 第三方 属性 ===================
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final GithubProperties github = new GithubProperties();

    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final WeiboProperties weibo = new WeiboProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final GiteeProperties gitee = new GiteeProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final DingtalkProperties dingtalk = new DingtalkProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final BaiduProperties baidu = new BaiduProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同. 注意：该平台已不支持
     */
    private final CsdnProperties csdn = new CsdnProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final CodingProperties coding = new CodingProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final OschinaProperties oschina = new OschinaProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final AlipayProperties alipay = new AlipayProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final QqProperties qq = new QqProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final WechatOpenProperties wechatOpen = new WechatOpenProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final WechatMpProperties wechatMp = new WechatMpProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final TaobaoProperties taobao = new TaobaoProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final GoogleProperties google = new GoogleProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final FacebookProperties facebook = new FacebookProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final DouyinProperties douyin = new DouyinProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final LinkedinProperties linkedin = new LinkedinProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final MicrosoftProperties microsoft = new MicrosoftProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final MiProperties mi = new MiProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final ToutiaoProperties toutiao = new ToutiaoProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final TeambitionProperties teambition = new TeambitionProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final RenrenProperties renren = new RenrenProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final PinterestProperties pinterest = new PinterestProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final StackOverflowProperties stackOverflow = new StackOverflowProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final HuaweiProperties huawei = new HuaweiProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final WechatEnterpriseProperties wechatEnterprise = new WechatEnterpriseProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final KujialeProperties kujiale = new KujialeProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final GitlabProperties gitlab = new GitlabProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final MeituanProperties meituan = new MeituanProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final ElemeProperties eleme = new ElemeProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final TwitterProperties twitter = new TwitterProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同. 注意：该平台暂时存在问题，请不要使用。待修复完成后会重新发版by yadong.zhang
     */
    private final FeishuProperties feishu = new FeishuProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final JdProperties jd = new JdProperties();
    /**
     * 字段名称与其所代表的第三方的 providerId 相同.
     */
    private final AliyunProperties aliyun = new AliyunProperties();


    // =================== OAuth2 属性 ===================
    /**
     * 抑制反射警告, 支持 JDK11, 默认: false ,
     * 在确认 WARNING: An illegal reflective access operation has occurred 安全后, 可以打开此设置, 可以抑制反射警告.
     */
    @Setter
    private Boolean suppressReflectWarning = false;

    /**
     * 是否支持第三方授权登录功能, 默认: true
     */
    @Setter
    private Boolean enabled = true;

    /**
     * 第三方授权登录后如未注册用户是否支持自动注册功能, 默认: true<br>
     * {@code https://gitee.com/pcore/just-auth-spring-security-starter/issues/I22KP3}
     */
    @Setter
    private Boolean autoSignUp = true;

    /**
     * 第三方授权登录后如未注册用户不支持自动注册功能, 则跳转到此 url 进行注册逻辑, 此 url 必须开发者自己实现; 默认: /signUp.html; <br>
     * 注意: 当 autoSignUp = false 时, 此属性才生效.<br>
     * 例如:<br>
     * 1. 设置值 "/signUp", 则跳转指定到 "/signUp" 进行注册. <br>
     * 2. 想返回自定义 json 数据到前端, 这里要设置 null , 在 {@link Auth2LoginAuthenticationFilter} 设置的
     * {@link AuthenticationSuccessHandler} 上处理返回 json; 判断是否为临时用户的条件是: {@link Authentication#getPrincipal()}
     * 是否为 {@link TemporaryUser} 类型.<br>
     */
    @Setter
    private String signUpUrl = "/signUp.html";

    /**
     * 第三方登录回调的域名, 例如：https://localhost 默认为 "http://127.0.0.1"，
     * redirectUrl 直接由 {domain}/{servletContextPath}/{redirectUrlPrefix}/{providerId}(ums.oauth.[qq/gitee/weibo])组成
     */
    @Setter
    private String domain = "http://127.0.0.1";

    /**
     * 第三方登录回调处理 url 前缀 ，也就是 RedirectUrl 的前缀, 不包含 ServletContextPath，默认为 /auth2/login.<br><br>
     */
    @Setter
    private String redirectUrlPrefix = "/auth2/login";

    /**
     * 第三方登录授权登录 url 前缀, 不包含 ServletContextPath，默认为 /auth2/authorization.<br><br>
     */
    @Setter
    private String authLoginUrlPrefix = "/auth2/authorization";

    /**
     * 第三方授权登录成功后的默认权限, 多个权限用逗号分开, 默认为: "ROLE_USER"
     */
    @Setter
    private String defaultAuthorities = "ROLE_USER";

    /**
     * 用于第三方授权登录时, 未开启自动注册且用户是第一次授权登录的临时用户密码, 默认为: "".<br>
     *     注意: 生产环境更换密码
     */
    @Setter
    private String temporaryUserPassword = "";
    /**
     * 用于第三方授权登录时, 未开启自动注册且用户是第一次授权登录的临时用户的默认权限, 多个权限用逗号分开, 默认为: "ROLE_TEMPORARY_USER"
     */
    @Setter
    private String temporaryUserAuthorities = "ROLE_TEMPORARY_USER";

    // =================== refreshToken 定时任务 属性 ===================
    /**
     * A cron-like expression.
     * <pre>
     * 0 * 2 * * ? 分别对应: second/minute/hour/day of month/month/day of week
     * </pre>
     * 默认为: "0 * 2 * * ?", 凌晨 2 点启动定时任务, 支持分布式(分布式 IOC 容器中必须有 {@link RedisConnectionFactory}, 也就是说,
     * 是否分布式执行依据 IOC 容器中是否有 {@link RedisConnectionFactory})
     * @see org.springframework.scheduling.support.CronSequenceGenerator
     */
    @Setter
    private String refreshTokenJobCron = "0 * 2 * * ?";

    /**
     * 是否支持定时刷新 AccessToken 定时任务. 默认: false.<br>
     * 支持分布式(分布式 IOC 容器中必须有 {@link RedisConnectionFactory}, 也就是说,
     * 是否分布式执行依据 IOC 容器中是否有 {@link RedisConnectionFactory})
     */
    @Setter
    private Boolean enableRefreshTokenJob = false;

    /**
     * 定时刷新 accessToken 任务时, 批处理数据库的记录数.<br>
     * 注意: 分布式应用时, 此配置不同服务器配置必须是一样的. batchCount 大小需要根据实际生产环境进行优化
     */
    @Setter
    private Integer batchCount = 1000;

    /**
     * accessToken 的剩余有效期内进行刷新 accessToken, 默认: 24, 单位: 小时.<br>
     * 注意: 需要根据实际生产环境进行优化
     */
    @Setter
    private Integer remainingExpireIn = 24;

    // =================== justAuth 属性 ===================

    @NestedConfigurationProperty
    private final JustAuthProperties justAuth = new JustAuthProperties();
    /**
     * 针对国外服务可以单独设置代理
     * HttpConfig config = new HttpConfig();
     * config.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10080)));
     * config.setTimeout(15000);
     *
     * @since 1.15.5
     */
    private final HttpConfigProperties proxy = new HttpConfigProperties();


    @Getter
    @Setter
    public static class HttpConfigProperties {

        /**
         * 当 enable = true 时, 返回 HttpConfig 对象, 否则返回为 null.
         * @return  当 enable = true 时, 返回 HttpConfig 对象, 否则返回为 null.
         */
        public HttpConfig getHttpConfig() {
            if (!enable)
            {
                return HttpConfig.builder().timeout((int) timeout.toMillis()).build();
            }
            return HttpConfig.builder()
                    .proxy(new Proxy(proxy, new InetSocketAddress(hostname, port)))
                    .timeout((int) timeout.toMillis())
                    .build();
        }

        /**
         * 是否支持代理, 默认为: false. <br>
         */
        private Boolean enable = false;

        /**
         * 针对国外服务可以单独设置代理类型, 默认 Proxy.Type.HTTP, enable = true 时生效.
         */
        private Proxy.Type proxy = Proxy.Type.HTTP;

        /**
         * 代理 host, enable = true 时生效.
         */
        private String hostname;

        /**
         * 代理端口, enable = true 时生效.
         */
        private Integer port;

        /**
         * 代理超时, 默认 PT3S
         */
        private Duration timeout = Duration.ofSeconds(3);
        /**
         * 用于国外网站代理超时, 默认 PT15S
         */
        private Duration foreignTimeout = Duration.ofSeconds(15);

    }

    @Getter
    @Setter
    public static class GithubProperties extends BaseAuth2Properties {
        /**
         * GITHUB 第三方服务商 id，默认是 github。
         */
        private String providerId = "github";

    }

    @Getter
    @Setter
    public static class WeiboProperties extends BaseAuth2Properties {
        /**
         * 新浪微博 第三方服务商 id，默认是 weibo。
         */
        private String providerId = "weibo";

    }

    @Getter
    @Setter
    public static class GiteeProperties extends BaseAuth2Properties {
        /**
         * gitee 服务提供商标识, 默认为 gitee
         */
        private String providerId = "gitee";

    }

    @Getter
    @Setter
    public static class DingtalkProperties extends BaseAuth2Properties {
        /**
         * 钉钉 第三方服务商 id，默认是 dingtalk。
         */
        private String providerId = "dingtalk";

    }

    @Getter
    @Setter
    public static class BaiduProperties extends BaseAuth2Properties {
        /**
         * 百度 第三方服务商 id，默认是 baidu。
         */
        private String providerId = "baidu";

    }

    @Getter
    @Setter
    public static class CsdnProperties extends BaseAuth2Properties {
        /**
         * csdn 第三方服务商 id，默认是 csdn。
         */
        private String providerId = "csdn";

    }

    @Getter
    @Setter
    public static class CodingProperties extends BaseAuth2Properties {
        /**
         * 服务提供商标识, 默认为 coding
         */
        private String providerId = "coding";
        /**
         * 使用 Coding 登录时，需要传该值。
         * <p>
         * 团队域名前缀，比如以“ https://justauth.coding.net/ ”为例，{@code codingGroupName} = justauth
         *
         * @since 1.15.5
         */
        private String codingGroupName;

    }

    @Getter
    @Setter
    public static class OschinaProperties extends BaseAuth2Properties {
        /**
         * OSCHINA 第三方服务商 id，默认是 oschina。
         */
        private String providerId = "oschina";

    }

    @Getter
    @Setter
    public static class AlipayProperties extends BaseAuth2Properties {
        /**
         * 支付宝 服务提供商标识, 默认为 alipay
         */
        private String providerId = "alipay";
        /**
         * 支付宝公钥：当选择支付宝登录时，该值可用
         * 对应“RSA2(SHA256)密钥”中的“支付宝公钥”
         */
        private String alipayPublicKey;

    }

    @Getter
    @Setter
    public static class QqProperties extends BaseAuth2Properties {
        /**
         * QQ 服务提供商标识, 默认为 qq
         */
        private String providerId = "qq";
        /**
         * 是否需要申请 unionId，默认: false. 目前只针对qq登录
         * 注：qq授权登录时，获取 unionId 需要单独发送邮件申请权限。如果个人开发者账号中申请了该权限，可以将该值置为true，在获取openId时就会同步获取unionId
         * 参考链接：http://wiki.connect.qq.com/unionid%E4%BB%8B%E7%BB%8D
         * <p>
         * 1.7.1版本新增参数
         */
        private Boolean unionId = false;
    }

    @Getter
    @Setter
    public static class WechatOpenProperties extends BaseAuth2Properties {
        /**
         * 微信开放平台 第三方服务商 id，默认是 wechatOpen。
         */
        private String providerId = "wechatOpen";

    }

    @Getter
    @Setter
    public static class WechatMpProperties extends BaseAuth2Properties {
        /**
         * 微信公众平台 第三方服务商 id，默认是 wechatMp。
         */
        private String providerId = "wechatMp";

    }

    @Getter
    @Setter
    public static class TaobaoProperties extends BaseAuth2Properties {
        /**
         * 淘宝 第三方服务商 id，默认是 taobao。
         */
        private String providerId = "taobao";

    }

    @Getter
    @Setter
    public static class GoogleProperties extends BaseAuth2Properties {
        /**
         * Google 第三方服务商 id，默认是 google。
         */
        private String providerId = "google";

    }
    @Getter
    @Setter
    public static class FacebookProperties extends BaseAuth2Properties {
        /**
         * Facebook 第三方服务商 id，默认是 facebook。
         */
        private String providerId = "facebook";

    }

    @Getter
    @Setter
    public static class DouyinProperties extends BaseAuth2Properties {
        /**
         * 抖音 第三方服务商 id，默认是 douyin。
         */
        private String providerId = "douyin";

    }

    @Getter
    @Setter
    public static class LinkedinProperties extends BaseAuth2Properties {
        /**
         * 领英 第三方服务商 id，默认是 linkedin。
         */
        private String providerId = "linkedin";

    }

    @Getter
    @Setter
    public static class MicrosoftProperties extends BaseAuth2Properties {
        /**
         * 微软 第三方服务商 id，默认是 microsoft。
         */
        private String providerId = "microsoft";

    }

    @Getter
    @Setter
    public static class MiProperties extends BaseAuth2Properties {
        /**
         * 小米 第三方服务商 id，默认是 mi。
         */
        private String providerId = "mi";

    }

    @Getter
    @Setter
    public static class ToutiaoProperties extends BaseAuth2Properties {
        /**
         * 今日头条 第三方服务商 id，默认是 toutiao。
         */
        private String providerId = "toutiao";

    }

    @Getter
    @Setter
    public static class TeambitionProperties extends BaseAuth2Properties {
        /**
         * Teambition 第三方服务商 id，默认是 teambition。
         */
        private String providerId = "teambition";

    }

    @Getter
    @Setter
    public static class RenrenProperties extends BaseAuth2Properties {
        /**
         * 人人网 第三方服务商 id，默认是 renren。
         */
        private String providerId = "renren";

    }

    @Getter
    @Setter
    public static class PinterestProperties extends BaseAuth2Properties {
        /**
         * Pinterest 第三方服务商 id，默认是 pinterest。
         */
        private String providerId = "pinterest";

    }

    @Getter
    @Setter
    public static class StackOverflowProperties extends BaseAuth2Properties {

        /**
         * Stack Overflow 服务提供商标识, 默认为 stackOverflow
         */
        private String providerId = "stackOverflow";

        /**
         * Stack Overflow Key
         * <p>
         *
         * @since 1.9.0
         */
        private String stackOverflowKey;

    }

    @Getter
    @Setter
    public static class HuaweiProperties extends BaseAuth2Properties {
        /**
         * 华为 第三方服务商 id，默认是 huawei。
         */
        private String providerId = "huawei";

    }

    /**
     * @author YongWu zheng
     */
    @Getter
    @Setter
    public static class WechatEnterpriseProperties extends BaseAuth2Properties {
        /**
         * 企业微信 第三方服务商 id，默认是 wechatEnterprise。
         */
        private String providerId = "wechatEnterprise";
        /**
         * 企业微信，授权方的网页应用ID
         *
         * @since 1.10.0
         */
        private String agentId;

    }

    @Getter
    @Setter
    public static class GitlabProperties extends BaseAuth2Properties {
        /**
         * Gitlab 第三方服务商 id，默认是 gitlab。
         */
        private String providerId = "gitlab";

    }

    @Getter
    @Setter
    public static class KujialeProperties extends BaseAuth2Properties {
        /**
         * 酷家乐 第三方服务商 id，默认是 kujiale。
         */
        private String providerId = "kujiale";

    }

    @Getter
    @Setter
    public static class ElemeProperties extends BaseAuth2Properties {
        /**
         * 饿了么 第三方服务商 id，默认是 eleme。
         */
        private String providerId = "eleme";

    }

    @Getter
    @Setter
    public static class MeituanProperties extends BaseAuth2Properties {
        /**
         * 美团 第三方服务商 id，默认是 meituan。
         */
        private String providerId = "meituan";

    }

    @Getter
    @Setter
    public static class TwitterProperties extends BaseAuth2Properties {
        /**
         * Twitter 第三方服务商 id，默认是 twitter。
         */
        private String providerId = "twitter";

    }

    @Getter
    @Setter
    public static class FeishuProperties extends BaseAuth2Properties {
        /**
         * 飞书 第三方服务商 id，默认是 feishu。
         */
        private String providerId = "feishu";

    }

    @Getter
    @Setter
    public static class JdProperties extends BaseAuth2Properties {
        /**
         * 京东 第三方服务商 id，默认是 jd。
         */
        private String providerId = "jd";

    }

    @Getter
    @Setter
    public static class AliyunProperties extends BaseAuth2Properties {
        /**
         * 阿里云 第三方服务商 id，默认是 aliyun。
         */
        private String providerId = "aliyun";

    }
}
