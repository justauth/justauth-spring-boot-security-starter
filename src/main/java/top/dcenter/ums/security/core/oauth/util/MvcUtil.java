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
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import top.dcenter.ums.security.core.oauth.config.Auth2AutoConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 功能: <br>
 * 1. 给 targetClass 的 methodName 方法上的 @Scheduled 的 cron 重新赋值为 cronValue<br>
 * 2. 获取 servletContextPath<br>
 * @author YongWu zheng
 * @version V1.0  Created by 2020/9/17 18:32
 */
@Slf4j
public class MvcUtil {

    /**
     * servletContextPath, 在应用启动时通过 {@link Auth2AutoConfiguration} 自动注入.
     */
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
    private static String servletContextPath = "";

    /**
     * 获取 servletContextPath
     * @return servletContextPath
     */
    public static String getServletContextPath() {
        return servletContextPath;
    }

    /**
     * jackson 封装
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 解决jackson2无法反序列化LocalDateTime的问题
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    /**
     * 通过 {@link ObjectMapper} 转换对象到 JSONString, 主要目的用于日志输出对象字符串时使用, 减少 try catch 嵌套, 转换失败记录日志并返回空字符串.
     * @param obj   Object
     * @return  返回 JSONString, 转换失败记录日志并返回空字符串.
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
     * 给 targetClass 的 methodName 方法上的 @Scheduled 的 cron 重新赋值为 cronValue
     *
     * @param methodName            method name
     * @param cronValue             corn value
     * @param targetClass           method 的 class
     * @param parameterTypes        the parameter array
     * @throws Exception    Exception
     */
    @SuppressWarnings("unchecked")
    public static void setScheduledCron(@NonNull String methodName, @NonNull String cronValue,
                                        @NonNull Class<?> targetClass, Class<?>... parameterTypes) throws Exception {

        Method method = targetClass.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);

        // 获取 annotationClass 注解
        final Scheduled annotation = method.getDeclaredAnnotation(Scheduled.class);
        if (null != annotation) {
            // 获取代理处理器
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
            // 获取私有 memberValues 属性
            Field memberValuesField = invocationHandler.getClass().getDeclaredField("memberValues");
            memberValuesField.setAccessible(true);
            // 获取实例的属性map
            Map<String, Object> memberValuesValue = (Map<String, Object>) memberValuesField.get(invocationHandler);
            // 修改属性值
            memberValuesValue.put("cron", cronValue);
        }
        else
        {
            String msg = String.format("设置 %s#%s() 方法的 cron 映射值时发生错误.",
                                       targetClass.getName(),
                                       methodName);
            log.error(msg);
            throw new RuntimeException(msg);
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