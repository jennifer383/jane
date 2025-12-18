package com.common.jane_ai.filter.StrategyImpl;

import com.common.jane_ai.filter.RequestValidationStrategy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
//直接通过的请求
public class RequestFreeAllowStrategyImpl implements RequestValidationStrategy {

    private static final List<String> FERR_ALLOW_PATTERNS = List.of(
            "/jane/register",// 注册
            "/jane/login",
            "/jane/pdf/ranking",
            "/jane/model", // 添加模型切换路径
            "/swagger-ui",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/webjars/**"
//            "/jane/chat"
    );

    @Override
    public boolean matching(String Path) {
        // 精确匹配或路径模式匹配
        for (String pattern : FERR_ALLOW_PATTERNS) {
            if (Path.equals(pattern) || (pattern.endsWith("/**") && Path.startsWith(pattern.substring(0, pattern.length() - 3)))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean execute(HttpServletRequest request, HttpServletResponse response) {
        //通行
        log.info("FREE_ALLOW---Acess");
        return true;
    }
}
