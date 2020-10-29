## 1.0.5
### Fixes and Improvements:
1. 添加了一些 Authentication 与 UserDetails 子类的反序列化器, 以解决 redis 缓存不能反序列化此类型的问题.
具体配置 redis 反序列器的配置请看 RedisCacheAutoConfiguration.getJackson2JsonRedisSerializer() 方法

## 1.0.4
### Fixes and Improvements:
1. 添加在不支持自动注册时, 创建临时用户 TemporaryUser 后跳转 signUpUrl, signUpUrl 可通过属性设置, 再次获取 TemporaryUser 通过 SecurityContextHolder.getContext().getAuthentication().getPrincipal().
2. 添加 UmsUserDetailsService.generateUsernames(AuthUser authUser) 接口默认实现方法, 便于开发者对与用户命名规则的自定义.
3. 更改接口 UmsUserDetailsService 的方法名称: existedByUserIds -> existedByUsernames. 更新方法说明.
4. 更新时序图, 更新 example 与 README.
5. 去除对 org.apache.commons.lang3.StringUtils 依赖.
6. 更新 JustAuth 到 1.15.8.

## 1.0.2
### Improvements:
1. 去除 JDK11 API, 增加对 JDK8的兼容性, 使用 JDK8 编译项目

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