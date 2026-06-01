package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice(basePackages = "com.example.demo.controller.web")
public class WebExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ModelAndView handleUserNotFound(UserNotFoundException ex) {
        log.warn("ユーザー未存在: {}", ex.getMessage());
        return new ModelAndView("error/404");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(
            Exception ex,
            HttpServletRequest request) {

        log.error("予期しないエラー: uri={}", request.getRequestURI(), ex);
        return new ModelAndView("error/500");
    }
}
