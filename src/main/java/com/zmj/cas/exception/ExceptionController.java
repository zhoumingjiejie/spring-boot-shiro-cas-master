package com.zmj.cas.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 0.0.0
 * @ProjectName: [spring-boot-shiro-cas-master]
 * @Package: [com.zmj.cas.exception.ExceptionController]
 * @Description 增强Controller
 * 1、全局异常处理
 * 2、全局数据绑定
 * 3、全局数据预处理
 * @Date 2020/3/28 23:08
 */
@Slf4j
@RestControllerAdvice
public class ExceptionController {

    /**
     * 处理访问方法时权限不足问题
     */
    @ExceptionHandler(value = UnauthorizedException.class)
    public String defaultErrorHandler(HttpServletRequest req, Exception e) {
        return "未经授权异常：".concat(e.getMessage());
    }
}
