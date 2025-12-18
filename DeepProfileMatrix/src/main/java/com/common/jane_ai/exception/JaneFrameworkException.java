package com.common.jane_ai.exception;

import com.common.jane_ai.common.ApiResponse;
import com.common.jane_ai.enums.BaseCode;
import lombok.Data;

@Data
public class JaneFrameworkException extends BaseException {

    private Integer code;
    private String message;

    public JaneFrameworkException() {
        super();
    }

    public JaneFrameworkException(String message) {
        super(message);
        this.message = message;
    }

    public JaneFrameworkException(String code, String message) {
        super(message);
        this.code = Integer.parseInt(code);
        this.message = message;
    }

    public JaneFrameworkException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public JaneFrameworkException(BaseCode baseCode) {
        super(baseCode.getMsg());
        this.code = baseCode.getCode();
        this.message = baseCode.getMsg();
    }

    public JaneFrameworkException(ApiResponse apiResponse) {
        super(apiResponse.getMessage());
        this.code = apiResponse.getCode();
        this.message = apiResponse.getMessage();
    }

    public JaneFrameworkException(Throwable cause) {
        super(cause);
    }

    public JaneFrameworkException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public JaneFrameworkException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}
