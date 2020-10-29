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
package demo.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.jackson2.CoreJackson2Module;
import org.springframework.security.web.jackson2.WebJackson2Module;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import top.dcenter.ums.security.core.oauth.config.RedisCacheAutoConfiguration;
import top.dcenter.ums.security.core.oauth.jackson.deserializes.Auth2Jackson2Module;
import top.dcenter.ums.security.core.oauth.service.UmsUserDetailsService;
import top.dcenter.ums.security.core.oauth.token.Auth2AuthenticationToken;
import top.dcenter.ums.security.core.oauth.util.MvcUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 演示 redis 添加反序列化配置.<br>
 * 目前添加了一些 Authentication 与 UserDetails 子类的反序列化器, 以解决 redis 缓存不能反序列化此类型的问题.<br>
 * 具体配置 redis 反序列器的配置请看 {@link RedisCacheAutoConfiguration}{@code .getJackson2JsonRedisSerializer()} 方法.<br>
 * 注意: {@link UmsUserDetailsService} 的注册用户方法返回的
 * {@link UserDetails} 的默认实现 {@link User} 已实现反序列化器, 如果是开发者自定义的子类, 需开发者自己实现反序列化器.
 * @author YongWu zheng
 * @version V2.0  Created by 2020/10/26 13:08
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Controller
public class DeserializerTestController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/user/me")
    @ResponseBody
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails,
                                              @SuppressWarnings("unused") HttpServletRequest request) throws JsonProcessingException {

        Map<String, Object> map = new HashMap<>(2);
        map.put("securityContextHolder", SecurityContextHolder.getContext().getAuthentication());
        map.put("userDetails", userDetails);

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info(MvcUtil.toJsonString(userDetails));

        // redis 添加反序列化配置.
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                                     ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModules(new CoreJackson2Module(), new WebJackson2Module(), new Auth2Jackson2Module());


        // 测试 redis 序列化 与 反序列化
        if (authentication instanceof Auth2AuthenticationToken)
        {
            Auth2AuthenticationToken auth2AuthenticationToken = (Auth2AuthenticationToken) authentication;

            stringRedisTemplate.opsForValue().set("testJsonDeserializer",
                                                  mapper.writeValueAsString(auth2AuthenticationToken));

            final String testJsonDeserializer = stringRedisTemplate.opsForValue().get("testJsonDeserializer");
            final Auth2AuthenticationToken token = mapper.readValue(testJsonDeserializer, Auth2AuthenticationToken.class);
            log.info("testJsonDeserializer: {}", token);

        }

        if (authentication instanceof AnonymousAuthenticationToken) {
            AnonymousAuthenticationToken aToken = (AnonymousAuthenticationToken) authentication;

            stringRedisTemplate.opsForValue().set("testJsonDeserializer",
                                                  mapper.writeValueAsString(aToken));

            final String testJsonDeserializer = stringRedisTemplate.opsForValue().get("testJsonDeserializer");
            final AnonymousAuthenticationToken token = mapper.readValue(testJsonDeserializer,
                                                                        AnonymousAuthenticationToken.class);
            log.info("testJsonDeserializer: {}", token);
        }


        return map;
    }

}
