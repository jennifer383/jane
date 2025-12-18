package com.common.jane_ai.common;


import com.common.jane_ai.enums.BaseCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
//一个接口里面什么内容都没有，我们可以将它理解成一个标识接口。
//比如在课堂上有位学生遇到一个问题，于是举手向老师请教，这时老师帮他解答，那么这位学生的举手其实就是一个标识，自己解决不了问题请教老师帮忙解决。
// 在Java中的这个Serializable接口其实是给jvm看的，通知jvm，我不对这个类做序列化了，你(jvm)帮我序列化就好了。
//Serializable接口就是Java提供用来进行高效率的异地共享实例对象的机制，实现这个接口即可。

/**
 * @description: 接口返回体基础类 dto
 * //spring 会默认把请求响应转为json格式
 **/
@Data
@Schema(title="ApiResponse", description ="数据响应规范结构")
public class ApiResponse<T> implements Serializable {
    
    @Schema(name ="code", type ="Integer", description ="响应码 0:成功 其余:失败")
    private Integer code;
    
    @Schema(name ="message", type ="String", description ="错误信息")
    private String message;
    
    @Schema(name ="data", description ="响应的具体数据")
    private T data;
    
    private ApiResponse() {}

    //          泛型的类型         返回值类型
    public static <T> ApiResponse<T> error(Integer code, String message) {
        ApiResponse<T> apiResponse = new ApiResponse<T>();
        apiResponse.code = code;
        apiResponse.message = message;
        return apiResponse;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> apiResponse = new ApiResponse<T>();
        apiResponse.code = -100;
        apiResponse.message = message;
        return apiResponse;
    }
    
    public static <T> ApiResponse<T> error(Integer code, T data) {
        ApiResponse<T> apiResponse = new ApiResponse<T>();
        apiResponse.code = -100;
        apiResponse.data = data;
        return apiResponse;
    }
    
    public static <T> ApiResponse<T> error(BaseCode baseCode) {
        ApiResponse<T> apiResponse = new ApiResponse<T>();
        apiResponse.code = baseCode.getCode();
        apiResponse.message = baseCode.getMsg();
        return apiResponse;
    }
    
    public static <T> ApiResponse<T> error(BaseCode baseCode, T data) {
        ApiResponse<T> apiResponse = new ApiResponse<T>();
        apiResponse.code = baseCode.getCode();
        apiResponse.message = baseCode.getMsg();
        apiResponse.data = data;
        return apiResponse;
    }

    public static <T> ApiResponse<T> error() {
        ApiResponse<T> apiResponse = new ApiResponse<T>();
        apiResponse.code = -100;
        apiResponse.message = "系统错误，请稍后重试!";
        return apiResponse;
    }
    
    public static <T> ApiResponse<T> ok() {
        ApiResponse<T> apiResponse = new ApiResponse<T>();
        apiResponse.code = 0;
        return apiResponse;
    }
    
    public static <T> ApiResponse<T> ok(T t) {
        ApiResponse<T> apiResponse = new ApiResponse<T>();
        apiResponse.code = 0;
        apiResponse.setData(t);
        return apiResponse;
    }
}
