package com.common.jane_ai.enums;

/**
 基础code码枚举类
 **/
public enum BaseCode {

    /**
     * 基础code码
     * */
    SUCCESS(0, "OK"), SYSTEM_ERROR(-1, "系统异常，请稍后重试"),

    USER_LOGIN_UNEXIST(301, "用户信息有误"),

    USER_REGISTER_EXIST(302, "用户信息已存在"),

    USER_LOGIN_PASSWORD_ERROR(303, "用户密码不正确"),

    RATE_LIMITER_EXCEPTION(304, "操作太频繁，请稍后再试"),

    YOUKE_CHAT_NUM_LIMIT_EXCEPTION(305, "游客登录会话次数已达限，如需使用请登录"),

    USER_TOKEN_NOEXIST_EXCEPTION(306, "用户登录已失效，请重新登录"),

    USER_TOKEN_NOVALID_EXCEPTION(306, "用户登录已失效，请重新登录"),

    USER_TOKEN_NO_AVALIABLE_EXCEPTION(307, "用户TOKEN异常，请重新登录"),

    MIDENG_NO_AVALIABLE_EXCEPTION(308, "请求频率过快，请稍后重试"),

    MIDENG_HAS_EXIST_EXCEPTION(308, "请求已接受，请稍后"),

    MIDENG_PDF_HAS_EXIST_EXCEPTION(308, "为了保证程序可用性，PDF文件上传需隔5min，请稍后重试"),

    YOUKE_CANT_TITLE_EXCEPTION(309, "游客不能生成Title"),

    REQUEST_UNUSABLE_EXCEPTION(309, "请求不可用"),

    PDF_UPLOAD_ERROR(310, "PDF解析失败"),

    RANKING_ERROR(310, "排行榜查询错误"),

    XXL_JOB_ERROR(310, "xxl-job执行错误");



    private final Integer code;

    private String msg = "";

    BaseCode(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode(){
        return this.code;
    }

    public String getMsg(){
        return this.msg == null ? "" : this.msg;
    }

    public static String getMsg(Integer code){
        for(BaseCode re : BaseCode.values()){
            if(re.code.intValue()== code.intValue()){
                return re.msg;
            }
        }
        return "";
    }

    public static BaseCode getRc(Integer code){
        for(BaseCode re : BaseCode.values()){
            if(re.code.intValue()== code.intValue()){
                return re;
            }
        }
        return null;
    }
}
