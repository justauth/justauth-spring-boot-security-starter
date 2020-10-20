/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.dcenter.ums.security.core.oauth.signup;

import me.zhyd.oauth.model.AuthUser;
import org.springframework.security.core.userdetails.UserDetails;
import top.dcenter.ums.security.core.oauth.entity.ConnectionData;
import top.dcenter.ums.security.core.oauth.exception.RegisterUserFailureException;
import top.dcenter.ums.security.core.oauth.repository.exception.UpdateConnectionException;

/**
 * A command that signs up a new user in the event no user id could be mapped from a {@link AuthUser}.
 * Allows for implicitly creating a local user profile from connection data during a provider sign-in attempt.
 * @author YongWu zheng
 * @version V2.0  Created by 2020-10-08 20:10
 */
public interface ConnectionService {

	/**
	 * Sign up a new user of the application from the connection.
	 * 如果 {@code authUser.getUsername()} 重名, 则使用 {@code authUser.getUsername() + "_" + authUser.getSource()} 或
	 * {@code authUser.getUsername() + "_" + authUser.getSource() +  "_" + authUser.getUuid()} 即
	 * username_{providerId}_{providerUserId}.
	 * @param authUser      the user info from the provider sign-in attempt
	 * @param providerId    第三方服务商, 如: qq, github
	 * @return the new user UserDetails. May be null to indicate that an implicit failed to register local user.
	 * @throws RegisterUserFailureException 用户重名或注册失败
	 */
	UserDetails signUp(AuthUser authUser, String providerId) throws RegisterUserFailureException;

	/**
	 * 根据传入的参数更新第三方授权登录的用户信息, 包括 accessToken 信息,
	 * @param authUser          {@link AuthUser}
	 * @param connectionData    第三方授权登录的用户信息
	 * @throws UpdateConnectionException    更新异常
	 */
	void updateUserConnection(final AuthUser authUser, final ConnectionData connectionData) throws UpdateConnectionException;

	/**
	 * 第三方授权登录信息{@link AuthUser}绑定到本地账号{@link UserDetails}, 且添加第三方授权登录信息到 user_connection 与 auth_token
	 *
	 * @param principal     本地用户数据
	 * @param authUser      第三方用户信息
	 * @param providerId    第三方服务商 Id
	 */
	void binding(UserDetails principal, AuthUser authUser, String providerId);
}
