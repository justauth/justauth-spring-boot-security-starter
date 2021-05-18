## 1.1.23
### Fixes and Improvements:
1. 修复: 未实现 OneClickService 时不能启动的问题.

## 1.1.22
### Fixes and Improvements:
1. 修复: redirect 时 url 中文乱码的bug. 感谢: jueding920.
2. 修复: 自定义第三方时: 获取 AuthScope 异常的bug.
3. 修复: Auth2JdbcUsersConnectionTokenRepository 不能动态替换 authTokenTableName 的bug. 感谢: jueding920.
4. 修复: RefreshTokenJobImpl.java NPE bug.
5. 修复: RefreshAccessTokenJobHandler.java NPE bug.
6. 新增: 获取当前账号下所有绑定的第三方账号接口, ConnectionService.listAllConnections() 接口.
7. 新增: 取消OAuth2的内置数据库的属性设置. ums.oauth.enableUserConnectionAndAuthTokenTable 与 ums.oauth.enableAuthTokenTable. 具体看: 取消OAuth2内置数据库说明.md.
8. 其他优化.

## 1.1.21
### Fixes and Improvements:
1. 修复: Issues:I393LI, 自定义scope不能覆盖默认scope 的 bug. 感谢 luodada.

## 1.1.20
### Fixes and Improvements:
1. 修复: yml 配置文件不能读取部分默认值的问题. 感谢 jueding920 与 a18739975021 提的bug.

## 1.1.19
### Fixes and Improvements:
1. 新增: 一键登录功能.
2. 添加: 一键登录 example.
3. 优化: 属性配置.

## 1.1.18
### Fixes and Improvements:
1. 优化: 第三方登录中 state 缓存 key 策略.
2. 优化: 依赖, 删除对 spring-security-oauth2-client 的依赖. 去除 spring-security-oauth2-resource-server 依赖的 scope:provider 标记.

## 1.1.17
### Fixes and Improvements:
1. 修复: 当本地登录用户为临时登录用户时绑定异常, 当本地登录用户的 Authentication 为 JwtAuthenticationToken 时绑定异常.
2. 添加: AuthenticationToUserDetailsConverter.java 接口并实现此接口.
3. 改进: 添加 expireIn2Timestamp 重载方法.

## 1.1.16
### Fixes and Improvements:
1. 修复: TemporaryUser 序列化问题. 完善第三方登录不支持自动注册功能的 example.
2. 改进: ConnectionService.java 针对第三方增加解绑接口并添加默认实现.
3. 改进: 添加对 BusinessException 的统一异常处理.

## 1.1.15
### Fixes and Improvements:
1. 修复: user_connection 与 auth_token 建表语句的bug.

## 1.1.14
### Fixes and Improvements:
1. 优化: 接口名称 updateUserConnectionAndAuthToken(..).
2. 改进: ConnectionService 接口添加 findConnectionByProviderIdAndProviderUserId(..) 方法, 这样 Auth2LoginAuthenticationProvider 只需引用 ConnectionService, 如需更改第三方用户信息的保存方式, 实现此类即可, 不再需要实现 UsersConnectionRepository 类.

## 1.1.13
### Fixes and Improvements:
1. 改进: 添加是否在启动时检查并自动创建 userConnectionTable 与 authTokenTable 控制开关属性.
2. 文档: 添加登录流程图.

## 1.1.12
### Fixes and Improvements:
1. 更新 JustAuth 依赖到 1.15.9 版本.
2. 新增 飞书, 喜马拉雅, 企业微信网页 第三方登录.
3. 增加 支付宝内置的代理自定义设置.
4. 升级 facebook api 版本到 9.0.
5. 修改 原来的企业微信为 企业微信二维码登录.
6. 修改: AuthToken 添加了 refreshTokenExpireIn 字段, 相应的修改数据库操作.
7. 删除: mdc 功能, 聚集第三方登录功能.
8. 重命名 ums.repository.tableName 为 ums.repository.userConnectionTableName.
9. 新增 auth_token 建表及查询表是否存在的 sql 语句属性(ums.repository.xxx).
10. 新增查询数据库名称的 sql 语句属性(ums.repository.queryDatabaseNameSql), 方便根据不同数据库自定义查询语句.
11. 优化: 删除 RepositoryProperties 无用的配置属性.
12. 示例: 修改配置.



## 1.1.11
### Fixes and Improvements:
1. 修复: 补全 AuthToken 与 UserConnection 建表语句, 与刷新 access Token 定时任务处理逻辑相匹配.
2. 改进: 添加 Auth2ControllerAdviceHandler 异常处理器且在返回异常信息中添加 MDC 调用链路追踪 ID.
3. 优化: ServletContextPath 获取方式.
4. 安全性: 在工具类/Holder 的类上添加 final 字段并把无参构造方法设置为 private.
5. 日志: 添加 MDC 统一的异常日志.


## 1.1.10
### Fixes and Improvements:
1. 改进: 使自定义第三方授权登录可以自定义 providerId.

## 1.1.9
### Fixes and Improvements:
1. release v1.1.9 优化 Auth2Properties.

## 1.1.8
### Fixes and Improvements:
1. release v1.1.8 优化 Auth2Properties 与 BaseAuth2Properties.

## 1.1.7
### Fixes and Improvements:
1. 改进: JustAuthProperties 中的 scopes 属性格式(providerId:scope), 使其可以针对不同的第三方服务商进行不同的自定义 scope 配置.
2. 改进: 初始化 details 逻辑, 方便自定义初始化 details.


## 1.1.6
### Fixes and Improvements:
1. 改进: 增加两个自定义 OAuth2 Login 入口与相对应的属性(ums.oauth.customize 和 ums.oauth.gitlabPrivate). 三个相关的抽象类 AuthCustomizeRequest/AuthCustomizeSource/AuthGitlabPrivateSource.
2. 示例: 增加自定义 OAuth2 Login 示例.
3. 日志: 增加异常日志.

## 1.1.5
### Fixes and Improvements:
1. 修复: 定时任务失效的问题. 主要因为 MdcScheduledThreadPoolTaskExecutor 覆写了 newTaskFor(..) 方法导致, 删除不必要的覆写方法, 简化其他的实现逻辑, 在增加 MDC
 功能的情况下尽量不影响原有方法的实现逻辑, 但是注意: remove(Runnable) 方法在类内部调用有效, 通过实例调用此方法失效.
2. 增强: 基于 SLF4J MDC 机制实现日志链路追踪功能: 增加自定义追踪 ID 属性配置及相应的接口.
3. 改进: 根据请求类型或接收的类型返回 Json 数据或网页.
4. 重构: 定时任务配置.
5. 优化: 数据库 redis 缓存删除重复设置过期时间语句.
6. 依赖: 设置springBoot:2.3.4使其与spring-security5.4.1版本匹配.



## 1.1.4
### Fixes and Improvements:
1. 修复: 不能加载部分第三方的 AuthDefaultRequest 的 bug

## 1.1.3
### Fixes and Improvements:
1. 优化: Auth2RequestHolder.getAuth2DefaultRequest(..) 与 Auth2RequestHolder.getProviderIdBySource(..) 方法.
2. 优化: 解决 log 中异常信息不详细.

## 1.1.2
### Fixes and Improvements:
1. 改进: 通过适配器模式对 AuthDefaultRequest 子类进行适配取代对 AuthDefaultRequest 子类的逐个继承的方式. 因 CSDN 与 FEISHU 不支持第三方授权登录故删除此第三方的支持.
2. 优化: 日志重复记录异常调用链的问题

## 1.1.1
### Fixes and Improvements:
1. 优化: 第三方授权登录获取授权链接时, 如果请求的第三方不在应用支持第三方服务商范围内, 跳转授权失败处理器处理.

## 1.1.0
### Fixes and Improvements:
1. 改进: 考虑到很多应用都有自己的定时任务应用, 提取 Executor 配置放入 executor 包, 从定时任务 RefreshAccessTokenJob 中拆分出 RefreshAccessTokenJobHandler
, RefreshTokenJob 接口的实现已注入 IOC 容器, 方便自定义定时任务接口时调用.
2. 依赖: 依赖升级到 spring-security:5.4.1, spring-boot:2.3.5.RELEASE.
3. 优化: 删除不必要的属性: ums.oauth.enabled.

## 1.0.12
### Fixes and Improvements:
1. 修复: 不能对部分通过 Filter 实现的逻辑进行 MDC 日志链路追踪的 bug, 如: 第三方授权登录, 因为 interceptor 拦截在 Filter 之后.

## 1.0.11
### Fixes and Improvements:
1. 修复: 第三方授权登录时, 缓存到 redis 时, 设置 state 缓存时间时少个时间单位, 变成 offset错误的 bug. 感谢: 永生的灯塔水母

## 1.0.10
### Fixes and Improvements:
1. 修复: enableRefreshTokenJob 属性不能控制是否开启定时刷新 accessToken 任务的 bug.

## 1.0.9
### Fixes and Improvements:
1. 修复: 示例功能, 当 signUpUrl=null 时, 成功处理器未生效的bug
2. 特性: 支持基于 SLF4J MDC 机制的日志链路追踪功能

## 1.0.8
### Fixes and Improvements:
1. 修复: 生成 userConnectionUpdateExecutor 时 maximumPoolSize 小于 corePoolSize 的 bug. 感谢: 永生的灯塔水母
2. 优化: 修改 signUpUrl 相关的注释,文档, 示例, 增加 signUp.html 提示页面

## 1.0.7
### Fixes and Improvements:
1. 修复: AuthStateRedisCache.java containsKey(key) 方法的 bug. 感谢: 永生的灯塔水母
2. 优化: signUpUrl 的处理方式: 增加如果 signUpUrl == null 时不跳转, 直接由开发者在成功处理器上自己处理.

## 1.0.6
### Fixes and Improvements:
1. 增强: RememberMeAuthenticationToken 的反序列化, 优化反序列化配置(Auth2Jackson2Module).

## 1.0.5
### Fixes and Improvements:
1. 增强: 添加了一些 Authentication 与 UserDetails 子类的反序列化器, 以解决 redis 缓存不能反序列化此类型的问题.
具体配置 redis 反序列器的配置请看 RedisCacheAutoConfiguration.getJackson2JsonRedisSerializer() 方法

## 1.0.4
### Fixes and Improvements:
1. 增强: 添加在不支持自动注册时, 创建临时用户 TemporaryUser 后跳转 signUpUrl, signUpUrl 可通过属性设置, 再次获取 TemporaryUser 通过 SecurityContextHolder.getContext().getAuthentication().getPrincipal().
2. 优化: 添加 UmsUserDetailsService.generateUsernames(AuthUser authUser) 接口默认实现方法, 便于开发者对与用户命名规则的自定义.
3. 优化: 更改接口 UmsUserDetailsService 的方法名称: existedByUserIds -> existedByUsernames. 更新方法说明.
4. 优化: 更新时序图, 更新 example 与 README.
5. 兼容: 去除对 org.apache.commons.lang3.StringUtils 依赖.
6. 改进: 更新 JustAuth 到 1.15.8.

## 1.0.2
### Improvements:
1. 兼容: 去除 JDK11 API, 增加对 JDK8的兼容性, 使用 JDK8 编译项目

## 1.0.0
### Fixes and Improvements:
1. 集成 JustAuth, 支持所有 JustAuth 支持的第三方授权登录，登录后自动注册或绑定。
2. 支持定时刷新 accessToken, 支持分布式定时任务。
3. 支持第三方授权登录的用户信息表与 token 信息表的缓存功能。
4. 支持第三方绑定与解绑及查询接口(top.dcenter.ums.security.core.oauth.repository.UsersConnectionRepository).
5. 支持线程池配置
6. 添加抑制非法反射警告, 适用于jdk11, 通过属性可以开启与关闭
7. 添加 spring cache 对 @Cacheable 操作异常处理, 缓存穿透处理(TTL随机20%上下浮动), 缓存击穿处理(添加对null值的缓存, 新增与更新时更新null值)
8. 移除 fastJson, 改成 Jackson.