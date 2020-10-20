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
package top.dcenter.ums.security.core.oauth.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.SpringSecurityCoreVersion;

import java.io.Serializable;

/**
 * A data transfer object that allows the internal state of a Connection to be persisted and transferred between layers of an application.
 * Some fields may be null .
 * For example, an OAuth2Connection has a null 'secret' field while an OAuth1Connection has null 'refreshToken' and 'expireTime' fields.
 * @author Keith Donald
 * @author YongWu zheng
 */
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionData implements Serializable {
	private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

	/**
	 * 本地用户id
	 */
	private String userId;
	/**
	 * 第三方服务商
	 */
	private String providerId;
	/**
	 * 第三方用户id
	 */
	private String providerUserId;

	/**
	 * userId 绑定同一个 providerId 的排序
	 */
	private Integer rank;
	/**
	 * 第三方用户名
	 */
	private String displayName;
	/**
	 * 主页
	 */
	private String profileUrl;
	/**
	 * 头像
	 */
	private String imageUrl;
	/**
	 * accessToken
	 */
	private String accessToken;
	/**
	 * auth_token.id
	 */
	private Long tokenId;
	/**
	 * refreshToken
	 */
	private String refreshToken;

	/**
	 * 过期日期, 基于 1970-01-01T00:00:00Z, 无过期时间默认为 -1
	 */
	private Long expireTime;

	/**
	 * 用户唯一 ID
	 * @return userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * The id of the provider the connection is associated with.
	 * @return The id of the provider the connection is associated with.
	 */
	public String getProviderId() {
		return providerId;
	}

	/**
	 * The id of the provider user this connection is connected to.
	 * @return The id of the provider user this connection is connected to.
	 */
	public String getProviderUserId() {
		return providerUserId;
	}

	/**
	 * userId 绑定同一个 providerId 的排序
	 * @return  rank
	 */
	@SuppressWarnings("unused")
	public Integer getRank() {
		return rank;
	}

	/**
	 * A display name for the connection.
	 * @return A display name for the connection.
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * A link to the provider's user profile page.
	 * @return A link to the provider's user profile page.
	 */
	public String getProfileUrl() {
		return profileUrl;
	}

	/**
	 * An image visualizing the connection.
	 * @return An image visualizing the connection.
	 */
	public String getImageUrl() {
		return imageUrl;
	}

	/**
	 * The access token required to make authorized API calls.
	 * @return The access token required to make authorized API calls.
	 */
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * The secret token needed to make authorized API calls.
	 * Required for OAuth1-based connections.
	 * @return The secret token needed to make authorized API calls.
	 */
	public Long getTokenId() {
		return tokenId;
	}

	/**
	 * A token use to renew this connection. Optional.
	 * Always null for OAuth1-based connections.
	 * @return A token use to renew this connection. Optional.
	 */
	public String getRefreshToken() {
		return refreshToken;
	}

	/**
	 * The time the connection expires. Optional.
	 * Always null for OAuth1-based connections.
	 * @return The time the connection expires. Optional.
	 */
	public Long getExpireTime() {
		return expireTime;
	}
		
}
