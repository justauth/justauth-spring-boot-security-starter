package demo.oneclicklogin.service;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import top.dcenter.ums.security.core.oauth.enums.ErrorCodeEnum;
import top.dcenter.ums.security.core.oauth.exception.Auth2Exception;
import top.dcenter.ums.security.core.oauth.oneclicklogin.service.OneClickLoginService;

import java.util.Map;

import static java.util.Objects.isNull;

/**
 * 一键登录服务实现: 从服务商获取手机号
 * @author YongWu zheng
 * @weixin z56133
 * @since 2021.5.13 17:50
 */
@Service
public class DemoOneClickLoginServiceImpl implements OneClickLoginService {
    @Override
    @NonNull
    public String callback(@NonNull String accessToken, @Nullable Map<String, String> otherParamMap) throws Auth2Exception {

        // 根据 accessToken 从服务商获取手机号 ...
        String mobile = "13345678981";

        //noinspection ConstantConditions
        if (isNull(mobile)) {
            throw new Auth2Exception(ErrorCodeEnum.QUERY_MOBILE_FAILURE_OF_ONE_CLICK_LOGIN, accessToken);
        }

        return mobile;
    }
}
