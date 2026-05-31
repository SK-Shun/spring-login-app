package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.dto.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public Object handleDuplicateEmail(
            DuplicateEmailException ex,
            HttpServletRequest request) {

        if (isApiRequest(request)) {
            log.warn("重複メールアドレス: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.of(
                            HttpStatus.CONFLICT.value(),
                            "DUPLICATE_EMAIL",
                            "このメールアドレスはすでに登録されています"
                    ));
        }
        return new ModelAndView("redirect:/register");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public Object handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request) {

        if (isApiRequest(request)) {
            log.warn("ユーザー未存在: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(
                            HttpStatus.NOT_FOUND.value(),
                            "USER_NOT_FOUND",
                            "ユーザーが見つかりません"
                    ));
        }
        return new ModelAndView("error/404");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("入力値が不正です");

        log.warn("バリデーションエラー: {}", message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION_ERROR",
                        message
                ));
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(
            Exception ex,
            HttpServletRequest request) {

        log.error("予期しないエラー: uri={}", request.getRequestURI(), ex);

        if (isApiRequest(request)) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "INTERNAL_SERVER_ERROR",
                            "サーバーエラーが発生しました"
                    ));
        }
        return new ModelAndView("error/500");
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }
}