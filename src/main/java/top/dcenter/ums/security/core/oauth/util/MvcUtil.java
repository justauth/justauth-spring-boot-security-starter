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

package top.dcenter.ums.security.core.oauth.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;
import top.dcenter.ums.security.core.oauth.consts.SecurityConstants;
import top.dcenter.ums.security.core.oauth.jackson.deserializes.SimpleGrantedAuthorityMixin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static top.dcenter.ums.security.core.oauth.consts.SecurityConstants.HEADER_ACCEPT;

/**
 * 功能: <br>
 * 1. 通过 ObjectMapper 转换对象到 JSON 字符串<br>
 * 2. 获取 servletContextPath<br>
 * @author YongWu zheng
 * @version V1.0  Created by 2020/9/17 18:32
 */
@Slf4j
public final class MvcUtil {

    private MvcUtil() { }

    /**
     * jackson 封装
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        OBJECT_MAPPER.addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityMixin.class);
        // 解决jackson2无法反序列化LocalDateTime的问题
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    /**
     * 通过 {@link ObjectMapper} 转换对象到 JSON 字符串, 主要目的用于日志输出对象字符串时使用, 减少 try catch 嵌套, 转换失败记录日志并返回空字符串.
     * @param obj   Object
     * @return  返回 JSON 字符串, 转换失败记录日志并返回空字符串.
     */
    public static String toJsonString(Object obj) {
        try
        {
            return OBJECT_MAPPER.writeValueAsString(obj);
        }
        catch (JsonProcessingException e)
        {
            String msg = String.format("Object2JsonString 失败: %s, Object=%s", e.getMessage(), obj);
            log.error(msg, e);
            return "";
        }
    }

    /**
     * 使用 {@link ObjectMapper} 把 jsonString 反序列化为 T 对象.
     * @param jsonString    json string
     * @param clz           要反序列化的目标 class
     * @return  返回反序列化对象, 如果反序列化错误返回 null
     */
    @Nullable
    public static <T> T json2Object(@NonNull String jsonString, @NonNull Class<T> clz) {
        try {
            return OBJECT_MAPPER.readValue(jsonString, clz);
        }
        catch (JsonProcessingException e) {
            log.error(String.format("[%s] 反序列化为 [%s] 时错误: %s", jsonString, clz.getName(), e.getMessage()), e);
            return null;
        }
    }

    public static final String HEADER_X_REQUESTED_WITH_NAME = "X-Requested-With";
    public static final String X_REQUESTED_WITH = "XMLHttpRequest";

    /**
     * 判断是否为 ajax 请求或者支持接收 json 格式
     * @param request   request
     * @return  但为 ajax 请求或者支持接收 json 格式返回 true
     */
    public static boolean isAjaxOrJson(HttpServletRequest request) {
        //判断是否为ajax请求 或 支持接收 json 格式
        String xRequestedWith = request.getHeader(HEADER_X_REQUESTED_WITH_NAME);
        String accept = request.getHeader(HEADER_ACCEPT);
        return (StringUtils.hasText(accept) && accept.contains(MediaType.APPLICATION_JSON_VALUE))
                || (xRequestedWith != null && xRequestedWith.equalsIgnoreCase(X_REQUESTED_WITH));
    }

    /**
     * 向客户端响应 json 格式
     * @param response  response
     * @param status    响应的状态码
     * @param result    相应的结果字符串
     * @throws IOException IOException
     */
    public static void responseWithJson(HttpServletResponse response, int status,
                                        String result) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(SecurityConstants.CHARSET_UTF8);
            PrintWriter writer = response.getWriter();
            writer.write(result);
            writer.flush();
        }
    }

    /**
     * Copy from {@code org.apache.commons.lang3.StringUtils}
     * <p>Splits a String by Character type as returned by
     * {@code java.lang.Character.getType(char)}. Groups of contiguous
     * characters of the same type are returned as complete tokens, with the
     * following exception: the character of type
     * {@code Character.UPPERCASE_LETTER}, if any, immediately
     * preceding a token of type {@code Character.LOWERCASE_LETTER}
     * will belong to the following token rather than to the preceding, if any,
     * {@code Character.UPPERCASE_LETTER} token.
     * <pre>
     * StringUtils.splitByCharacterTypeCamelCase(null)         = null
     * StringUtils.splitByCharacterTypeCamelCase("")           = []
     * StringUtils.splitByCharacterTypeCamelCase("ab de fg")   = ["ab", " ", "de", " ", "fg"]
     * StringUtils.splitByCharacterTypeCamelCase("ab   de fg") = ["ab", "   ", "de", " ", "fg"]
     * StringUtils.splitByCharacterTypeCamelCase("ab:cd:ef")   = ["ab", ":", "cd", ":", "ef"]
     * StringUtils.splitByCharacterTypeCamelCase("number5")    = ["number", "5"]
     * StringUtils.splitByCharacterTypeCamelCase("fooBar")     = ["foo", "Bar"]
     * StringUtils.splitByCharacterTypeCamelCase("foo200Bar")  = ["foo", "200", "Bar"]
     * StringUtils.splitByCharacterTypeCamelCase("ASFRules")   = ["ASF", "Rules"]
     * </pre>
     * @param str the String to split, may be {@code null}
     * @param camelCase whether to use so-called "camel-case" for letter types
     * @return an array of parsed Strings, {@code null} if null String input
     * @since 2.4
     */
    public static String[] splitByCharacterTypeCamelCase(final String str, final boolean camelCase) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return new String[0];
        }
        final char[] c = str.toCharArray();
        final List<String> list = new ArrayList<>();
        int tokenStart = 0;
        int currentType = Character.getType(c[tokenStart]);
        for (int pos = tokenStart + 1; pos < c.length; pos++) {
            final int type = Character.getType(c[pos]);
            if (type == currentType) {
                continue;
            }
            if (camelCase && type == Character.LOWERCASE_LETTER && currentType == Character.UPPERCASE_LETTER) {
                final int newTokenStart = pos - 1;
                if (newTokenStart != tokenStart) {
                    list.add(new String(c, tokenStart, newTokenStart - tokenStart));
                    tokenStart = newTokenStart;
                }
            } else {
                list.add(new String(c, tokenStart, pos - tokenStart));
                tokenStart = pos;
            }
            currentType = type;
        }
        list.add(new String(c, tokenStart, c.length - tokenStart));
        return list.toArray(new String[0]);
    }

}