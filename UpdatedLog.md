## 1.0.10
### Fixes and Improvements:
1. 修复: 修复 enableRefreshTokenJob 属性不能控制是否开启定时刷新 accessToken 任务的 bug

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