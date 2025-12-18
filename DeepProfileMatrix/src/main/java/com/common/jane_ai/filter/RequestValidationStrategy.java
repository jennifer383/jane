package com.common.jane_ai.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public interface RequestValidationStrategy {
    //与请求的路径相匹配
    boolean matching(String Path);
    //请求类型对应执行逻辑
    boolean execute(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
