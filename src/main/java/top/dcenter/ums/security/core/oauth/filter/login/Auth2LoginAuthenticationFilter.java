/*
 * Copyright 2002-2019 the original author or authors.
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
package top.dcenter.ums.security.core.oauth.filter.login;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import top.dcenter.ums.security.core.oauth.filter.redirect.Auth2DefaultRequestResolver;
import top.dcenter.ums.security.core.oauth.justauth.request.Auth2DefaultRequest;
import top.dcenter.ums.security.core.oauth.repository.Auth2DefaultRequestRepository;
import top.dcenter.ums.security.core.oauth.repository.HttpSessionAuth2DefaultRequestRepository;
import top.dcenter.ums.security.core.oauth.token.Auth2LoginAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An implementation of an {@link AbstractAuthenticationProcessingFilter} for OAuth 2.0
 * Login.
 *
 * <p>
 * This authentication {@code Filter} handles the processing of an OAuth 2.0 Authorization
 * Response for the authorization code grant flow and delegates an
 * {@link OAuth2LoginAuthenticationToken} to the {@link AuthenticationManager} to log in
 * the End-User.
 *
 * <p>
 * The OAuth 2.0 Authorization Response is processed as follows:
 *
 * <ul>
 * <li>Assuming the End-User (Resource Owner) has granted access to the Client, the
 * Authorization Server will append the {@link OAuth2ParameterNames#CODE code} and
 * {@link OAuth2ParameterNames#STATE state} parameters to the
 * {@link OAuth2ParameterNames#REDIRECT_URI redirect_uri} (provided in the Authorization
 * Request) and redirect the End-User's user-agent back to this {@code Filter} (the
 * Client).</li>
 * <li>This {@code Filter} will then create an {@link OAuth2LoginAuthenticationToken} with
 * the {@link OAuth2ParameterNames#CODE code} received and delegate it to the
 * {@link AuthenticationManager} to authenticate.</li>
 * <li>Upon a successful authentication, an {@link OAuth2AuthenticationToken} is created
 * (representing the End-User {@code Principal}) and associated to the
 * {@link OAuth2AuthorizedClient Authorized Client} using the
 * {@link OAuth2AuthorizedClientRepository}.</li>
 * <li>Finally, the {@link OAuth2AuthenticationToken} is returned and ultimately stored in
 * the {@link SecurityContextRepository} to complete the authentication processing.</li>
 * </ul>
 *
 * @author Joe Grandja
 * @since 5.0
 * @see AbstractAuthenticationProcessingFilter
 * @see OAuth2LoginAuthenticationToken
 * @see OAuth2AuthenticationToken
 * @see OAuth2LoginAuthenticationProvider
 * @see OAuth2AuthorizationRequest
 * @see OAuth2AuthorizationResponse
 * @see AuthorizationRequestRepository
 * @see OAuth2AuthorizationRequestRedirectFilter
 * @see ClientRegistrationRepository
 * @see OAuth2AuthorizedClient
 * @see OAuth2AuthorizedClientRepository
 * @see <a target="_blank" href="https://tools.ietf.org/html/rfc6749#section-4.1">Section
 * 4.1 Authorization Code Grant</a>
 * @see <a target="_blank" href=
 * "https://tools.ietf.org/html/rfc6749#section-4.1.2">Section 4.1.2 Authorization
 * Response</a>
 */
@SuppressWarnings("JavaDoc")
public class Auth2LoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String AUTHORIZATION_REQUEST_NOT_FOUND_ERROR_CODE = "authorization_request_not_found";

    private Auth2DefaultRequestRepository<Auth2DefaultRequest> authorizationRequestRepository =
            new HttpSessionAuth2DefaultRequestRepository();

    private final Auth2DefaultRequestResolver authorizationRequestResolver;

    /**
     * Constructs an {@code Auth2LoginAuthenticationFilter} using the provided
     * parameters.
     * @param filterProcessesUrl the {@code URI} where this {@code Filter} will process
     * the authentication requests, not null
     * @since 5.1
     */
    public Auth2LoginAuthenticationFilter(@NonNull String filterProcessesUrl) {
        super(filterProcessesUrl + "/*");
        this.authorizationRequestResolver = new Auth2DefaultRequestResolver(filterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        MultiValueMap<String, String> params = Auth2AuthorizationResponseUtils.toMultiMap(request.getParameterMap());
        if (!Auth2AuthorizationResponseUtils.isAuthorizationResponse(params)) {
            OAuth2Error oauth2Error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }
        Auth2DefaultRequest auth2DefaultRequest = this.authorizationRequestRepository.removeAuthorizationRequest(request, response);
        if (auth2DefaultRequest == null) {
            String registrationId = this.authorizationRequestResolver.resolveRegistrationId(request);
            OAuth2Error oauth2Error = new OAuth2Error(AUTHORIZATION_REQUEST_NOT_FOUND_ERROR_CODE,
                                                      "Client Registration not found with Id: " + registrationId, null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }

        Object authenticationDetails = this.authenticationDetailsSource.buildDetails(request);
        Auth2LoginAuthenticationToken authenticationRequest = new Auth2LoginAuthenticationToken(auth2DefaultRequest,
                                                                                                request);
        authenticationRequest.setDetails(authenticationDetails);

        // 通过 AuthenticationManager 转到相应的 Provider 对 Auth2LoginAuthenticationToken 进行认证
        return  this.getAuthenticationManager().authenticate(authenticationRequest);
    }

    /**
     * Sets the repository for stored {@link OAuth2AuthorizationRequest}'s.
     * @param authorizationRequestRepository the repository for stored
     * {@link OAuth2AuthorizationRequest}'s
     */
    @SuppressWarnings("unused")
    public final void setAuthorizationRequestRepository(
            Auth2DefaultRequestRepository<Auth2DefaultRequest> authorizationRequestRepository) {
        Assert.notNull(authorizationRequestRepository, "authorizationRequestRepository cannot be null");
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

}
