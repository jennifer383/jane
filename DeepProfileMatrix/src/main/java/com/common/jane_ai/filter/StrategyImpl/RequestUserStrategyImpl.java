package com.common.jane_ai.filter.StrategyImpl;

import com.common.jane_ai.common.BCryptPasswordUtils;
import com.common.jane_ai.common.ResponseUtils;
import com.common.jane_ai.enums.BaseCode;
import com.common.jane_ai.filter.RequestValidationStrategy;
import com.common.jane_ai.jwt.JwtUtils;
import com.common.jane_ai.threadlocal.BaseParameterHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.common.jane_ai.constant.JaneTypeConstant.*;

@Component
@Slf4j
public class RequestUserStrategyImpl implements RequestValidationStrategy {

    private static final List<String> USER_PATTERNS = List.of(
            "/jane/chat"//回话
           , "/jane/model"//回话
            ,"/jane/titlehistory"//会话历史记录Title
            ,"/jane/chathistory"
            ,"/jane/pdf/upload"
    );
    @Override
    public boolean matching(String Path) {
        String userIdStr = BaseParameterHolder.getBaseParameter(USER_ID);
        // 添加空指针和空字符串检查，避免NumberFormatException
        if(USER_PATTERNS.contains(Path) && userIdStr != null && !userIdStr.isEmpty()){
            try {
                int userId = Integer.parseInt(userIdStr);
                return userId != 0;
            } catch (NumberFormatException e) {
                log.error("Invalid userId format: {}", userIdStr);
                return false;
            }
        }
        return false;
    }
    //🔍 简洁结论
    //方法	优点	缺点	相对性能
    //✅ JWT 本地校验（不存状态）	无需 IO、纯 CPU 校验，速度非常快	无法主动失效 token，安全性较低	最快（仅需微秒级）
    //✅ Redis 状态校验（存储 token）	可以随时使 token 失效，灵活控制会话状态	有一次网络 IO（即使本地 Redis）	快（毫秒级），但比纯 JWT 慢几个数量级

    @Override
    public boolean execute(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("USER_STRATEGY---Acess");
        String token = BaseParameterHolder.getBaseParameter(AUTHORIZATION);
        String userId = BaseParameterHolder.getBaseParameter(USER_ID);
        if(StringUtils.isEmpty(token)){
            log.error("Token is empty");
            ResponseUtils.writeError(response, BaseCode.USER_TOKEN_NOEXIST_EXCEPTION);
            return false;
        }
        String jwtUserId ="";
        try {
            Map<String, Object> claims = JwtUtils.parseJWT(token);
            log.info("USER_CELUE-1-"+Thread.currentThread().getName());
            // 添加空指针检查，避免NullPointerException
            Object userIdObj = claims.get(USER_ID);
            if (userIdObj == null) {
                log.error("userId not found in JWT claims");
                ResponseUtils.writeError(response, BaseCode.USER_TOKEN_NOVALID_EXCEPTION);
                return false;
            }
            jwtUserId = userIdObj.toString();
            //在登录时在redis中存放了用户id，可以进行拓展
        } catch (Exception e) {
            log.error("Token parsing failed for userId-{}, error: {}", userId, e.getMessage());
            ResponseUtils.writeError(response, BaseCode.USER_TOKEN_NOVALID_EXCEPTION);
            return false;
        }
        log.info("用户token解析成功-userId-{}",userId);
        if(jwtUserId == null || !jwtUserId.equals(userId)){
            log.error("Token validation failed: jwtUserId={}, userId={}", jwtUserId, userId);
            ResponseUtils.writeError(response, BaseCode.USER_TOKEN_NO_AVALIABLE_EXCEPTION);
            return false;
        }
        return true;
    }
}
