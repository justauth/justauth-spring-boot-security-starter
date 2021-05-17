# 取消 OAuth2 的内置数据库说明

## 一. 同时取消第三方登录的 user_connection 与 auth_token 表
### 1. 属性配置 

```yaml
ums:
  oauth:
    # 是否支持内置的第三方登录用户表(user_connection) 和 auth_token 表. 默认: true.
    # 注意: 如果为 false, 则必须重新实现 ConnectionService 接口.
    enable-user-connection-and-auth-token-table: false
```
### 2. 必须重新实现 `top.dcenter.ums.security.core.oauth.signup.ConnectionService` 接口

## 二. 取消第三方登录 auth_token 表
### 1. 属性配置 

```yaml
ums:
  oauth:
    # 是否支持内置的第三方登录 token 表(auth_token). 默认: true.
    enable-auth-token-table: false
```