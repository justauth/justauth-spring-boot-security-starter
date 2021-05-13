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
package top.dcenter.ums.security.core.oauth.oneclicklogin;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import top.dcenter.ums.security.core.oauth.service.UmsUserDetailsService;


/**
 * 一键登录 provider
 * @author YongWu zheng
 * @weixin z56133
 * @since 2021.5.13 15:16
 */
public class OneClickLoginAuthenticationProvider implements AuthenticationProvider {
    private final UmsUserDetailsService userDetailsService;

    public OneClickLoginAuthenticationProvider(@NonNull UmsUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }
        OneClickLoginAuthenticationToken authenticationToken = (OneClickLoginAuthenticationToken) authentication;

        if (authentication.isAuthenticated())
        {
            return authentication;
        }

        UserDetails user;
        try {
            user = this.userDetailsService.loadUserByUsername((String) authenticationToken.getPrincipal());
        }
        catch (UsernameNotFoundException e) {
            user = null;
        }

        if (user == null)
        {
            user = this.userDetailsService.registerUser((String) authenticationToken.getPrincipal(),
                                                        authenticationToken.getOtherParamMap());

        }
        OneClickLoginAuthenticationToken authenticationResult =
                new OneClickLoginAuthenticationToken(user,
                                                     authenticationToken.getOtherParamMap(),
                                                     user.getAuthorities());
        authenticationResult.setDetails(authenticationToken.getDetails());
        return authenticationResult;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OneClickLoginAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
