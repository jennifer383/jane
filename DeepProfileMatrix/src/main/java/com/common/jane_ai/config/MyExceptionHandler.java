package com.common.jane_ai.config;


import com.common.jane_ai.common.ApiResponse;
import com.common.jane_ai.exception.JaneFrameworkException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.validation.BindException;
import java.io.IOException;
import java.util.stream.Collectors;

//@ResponseBody和@ControllerAdvice的组合  自动转化为json数据
//全局异常处理器
@Slf4j
@RestControllerAdvice(basePackages = "com.common.jane_ai.controller")
public class MyExceptionHandler {
    /**
     * 自定义异常
     * @param e
     * @return
     */
    @ExceptionHandler(JaneFrameworkException.class)
    public ApiResponse handleJaneFrameworkException(JaneFrameworkException e){
        return ApiResponse.error(e.getCode(),e.getMessage());
    }

    /**
     * @NotBlank注解 为""或者"  "
     * @param e
     * @return
     */////处理请求参数格式错误 @RequestBody上validate失败后抛出的异常是MethodArgumentNotValidException异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        return ApiResponse.error(e.getBindingResult().getFieldError().getDefaultMessage());
    }

    ////处理请求参数格式错误 @RequestParam上validate失败后抛出的异常是javax.validation.ConstraintViolationException
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse handleConstraintViolationException(ConstraintViolationException e){
        return ApiResponse.error(e.getConstraintViolations().iterator().next().getMessage());
    }

    //处理Get请求中 使用@Valid 验证路径中请求实体校验失败后抛出的异常
    @ExceptionHandler(BindException.class)
    public ApiResponse BindExceptionHandler(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ApiResponse.error(message);
    }

    // 处理运行时异常
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException occurred", e);
        return ApiResponse.error("服务器内部错误：" + e.getMessage());
    }

    // 处理IO异常
    @ExceptionHandler(IOException.class)
    public ApiResponse handleIOException(IOException e) {
        return ApiResponse.error("IO异常：" + e.getMessage());
    }

    // 处理所有未捕获的异常
    @ExceptionHandler(Exception.class)
    public ApiResponse handleException(Exception e) {
        return ApiResponse.error("服务器内部错误，请稍后重试");
    }
}
