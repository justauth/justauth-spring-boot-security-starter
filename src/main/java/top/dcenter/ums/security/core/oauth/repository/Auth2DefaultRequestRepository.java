/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.dcenter.ums.security.core.oauth.repository;

import top.dcenter.ums.security.core.oauth.filter.login.Auth2LoginAuthenticationFilter;
import top.dcenter.ums.security.core.oauth.filter.redirect.Auth2DefaultRequestRedirectFilter;
import top.dcenter.ums.security.core.oauth.justauth.request.Auth2DefaultRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implementations of this interface are responsible for the persistence of
 * {@link Auth2DefaultRequest} between requests.
 *
 * <p>
 * Used by the {@link Auth2DefaultRequestRedirectFilter} for persisting the
 * {@link Auth2DefaultRequest} before it initiates the authorization code grant flow. As well,
 * used by the {@link Auth2LoginAuthenticationFilter} for resolving the associated
 * {@link Auth2DefaultRequest} when handling the callback of the Authorization Response.
 *
 * @param <T> The type of OAuth 2.0 Auth2DefaultRequest
 * @author Joe Grandja
 * @author YongWu zheng
 * @see Auth2DefaultRequest
 * @see HttpSessionAuth2DefaultRequestRepository
 */
public interface Auth2DefaultRequestRepository<T extends Auth2DefaultRequest> {

	/**
	 * Returns the {@link Auth2DefaultRequest} associated to the provided
	 * {@code HttpServletRequest} or {@code null} if not available.
	 * @param request the {@code HttpServletRequest}
	 * @return the {@link Auth2DefaultRequest} or {@code null} if not available
	 */
	@SuppressWarnings("unused")
	T loadAuthorizationRequest(HttpServletRequest request);

	/**
	 * Persists the {@link Auth2DefaultRequest} associating it to the provided
	 * {@code HttpServletRequest} and/or {@code HttpServletResponse}.
	 * @param authDefaultRequest the {@link Auth2DefaultRequest}
	 * @param request the {@code HttpServletRequest}
	 * @param response the {@code HttpServletResponse}
	 */
	void saveAuthorizationRequest(T authDefaultRequest, HttpServletRequest request, HttpServletResponse response);

	/**
	 * Removes and returns the {@link Auth2DefaultRequest} associated to the
	 * provided {@code HttpServletRequest} and {@code HttpServletResponse} or if not
	 * available returns {@code null}.
	 * @param request the {@code HttpServletRequest}
	 * @param response the {@code HttpServletResponse}
	 * @return the {@link Auth2DefaultRequest} or {@code null} if not available
	 * @since 5.1
	 */
	T removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response);

}
