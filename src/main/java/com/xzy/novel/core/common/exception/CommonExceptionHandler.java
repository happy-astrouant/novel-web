package com.xzy.novel.core.common.exception;

import com.xzy.novel.core.common.constant.ErrorCodeEnum;
import com.xzy.novel.core.common.resp.RestResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 通用的异常处理器
 *
 * @RestControllerAdvice 会拦截所有@Controller
 *   搭配@ExceptionHandler 会拦截所有Controller抛出的异常
 *   当然也可以用于实现其他增强和拦截作用
 */
@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    /**
     * 处理数据校验异常
     * */
    @ExceptionHandler(BindException.class)
    public RestResp<Void> handlerBindException(BindException e){
        log.error(e.getMessage(),e);
        return RestResp.fail(ErrorCodeEnum.USER_REQUEST_PARAM_ERROR);
    }

    /**
     * 处理业务异常
     * */
    @ExceptionHandler(BusinessException.class)
    public RestResp<Void> handlerBusinessException(BusinessException e){
        log.error(e.getMessage(),e);
        return RestResp.fail(e.getErrorCodeEnum());
    }

    /**
     * 处理系统异常
     * */
    @ExceptionHandler(Exception.class)
    public RestResp<Void> handlerException(Exception e){
        log.error(e.getMessage(),e);
        return RestResp.error();
    }

}

