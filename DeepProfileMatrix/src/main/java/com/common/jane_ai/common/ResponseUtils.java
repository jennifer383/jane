package com.common.jane_ai.common;

import com.common.jane_ai.enums.BaseCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

//过滤器中的的自定义异常返回
public class ResponseUtils {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static void writeError(HttpServletResponse response, BaseCode baseCode) throws IOException {
        // 设置CORS头，确保浏览器允许前端接收响应
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept, Origin");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        //HTTP 状态码只代表通信层的状态（如请求有没有成功到达服务器、是否超时等）；
        //实际的业务状态由接口响应体中的自定义 code 字段表示（比如你用的 BaseCode 枚举）
//        HTTP 状态码统一为 200，业务状态码自定义
        response.setStatus(200);//上面的都是响应头
        ApiResponse apiResponse = ApiResponse.error(baseCode);
        //而getWriter().write()设置的是响应体（Response Body），这是实际返回给客户端的数据内容
        response.getWriter().write(MAPPER.writeValueAsString(apiResponse));
    }
}
