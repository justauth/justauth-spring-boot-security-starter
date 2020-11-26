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
package top.dcenter.ums.security.core.mdc.filter;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;
import top.dcenter.ums.security.core.mdc.MdcIdType;
import top.dcenter.ums.security.core.mdc.MdcIdGenerator;
import top.dcenter.ums.security.core.mdc.properties.MdcProperties;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static top.dcenter.ums.security.core.mdc.utils.MdcUtil.getMdcId;

/**
 * 基于 SLF4J MDC 机制实现日志链路追踪: 在输出日志中加上 MDC_TRACE_ID
 * @author YongWu zheng
 * @version V2.0  Created by 2020/10/31 18:19
 */
public class MdcLogFilter extends OncePerRequestFilter {

    /**
     * 在输出日志中加上指定的 MDC_TRACE_ID
     */
    public static final String MDC_KEY = "MDC_TRACE_ID";

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired(required = false)
    private MdcIdGenerator mdcIdGenerator;

    private final Set<String> includeUrls;
    private final Set<String> excludeUrls;
    private final AntPathMatcher matcher;
    private final UrlPathHelper helper;
    private final MdcIdType idType;

    public MdcLogFilter(MdcProperties mdcProperties) {
        this.idType = mdcProperties.getType();
        this.matcher = new AntPathMatcher();
        this.includeUrls = new HashSet<>();
        this.excludeUrls = new HashSet<>();

        this.helper = new UrlPathHelper();
        helper.setAlwaysUseFullPath(true);

        includeUrls.addAll(mdcProperties.getIncludeUrls());

        final Set<String> excludeUrls = mdcProperties.getExcludeUrls();
        if (null != excludeUrls) {
            this.excludeUrls.addAll(excludeUrls);
        }
    }


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (isEnableMdc(request)) {
            String token = getMdcId(idType, mdcIdGenerator);
            MDC.put(MDC_KEY, token);
            filterChain.doFilter(request, response);
            MDC.clear();
            return;
        }

        filterChain.doFilter(request, response);

    }

    private boolean isEnableMdc(HttpServletRequest request) {
        final String requestUri = this.helper.getPathWithinApplication(request);
        for (String excludeUrl : this.excludeUrls) {
            if (this.matcher.match(excludeUrl, requestUri)) {
                return false;
            }
        }
        for (String includeUrl : this.includeUrls) {
            if (this.matcher.match(includeUrl, requestUri)) {
                return true;
            }
        }
        return false;
    }

}