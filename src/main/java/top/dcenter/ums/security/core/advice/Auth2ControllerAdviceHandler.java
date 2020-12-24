package top.dcenter.ums.security.core.advice;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import top.dcenter.ums.security.core.oauth.exception.AbstractResponseJsonAuthenticationException;
import top.dcenter.ums.security.core.oauth.exception.Auth2Exception;
import top.dcenter.ums.security.core.oauth.exception.RefreshTokenFailureException;
import top.dcenter.ums.security.core.vo.ResponseResult;

import static top.dcenter.ums.security.core.mdc.utils.MdcUtil.getMdcTraceId;

/**
 * OAuth2 异常处理器
 * @author YongWu zheng
 * @version V2.0  Created by 2020.12.21 21:58
 */
@Order(100)
@ControllerAdvice
public class Auth2ControllerAdviceHandler {

    @ExceptionHandler(AbstractResponseJsonAuthenticationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseResult abstractResponseJsonAuthenticationException(AbstractResponseJsonAuthenticationException ex) {
        String errorMsg = ex.getMessage();
        return ResponseResult.fail(errorMsg, ex.getErrorCodeEnum(), getMdcTraceId());
    }

    @ExceptionHandler(Auth2Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public ResponseResult auth2Exception(Auth2Exception ex) {
        return ResponseResult.fail(ex.getMessage(), ex.getErrorCodeEnum(), getMdcTraceId());
    }

    @ExceptionHandler(RefreshTokenFailureException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseResult refreshTokenFailureException(RefreshTokenFailureException ex) {
        return ResponseResult.fail(ex.getMessage(), ex.getErrorCodeEnum(), getMdcTraceId());
    }
}