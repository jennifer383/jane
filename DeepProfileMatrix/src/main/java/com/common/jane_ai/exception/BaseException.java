package com.common.jane_ai.exception;

//BaseException存在的意义是作为业务异常的基类
//          五个异常处理方法
public class BaseException extends RuntimeException{

    public BaseException() {

    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(Integer code, String message, Throwable cause) {
        super(message, cause);
    }
}
