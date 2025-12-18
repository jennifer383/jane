package com.common.jane_ai.constant;

public class JaneRedisKeyConstant {

    public static final Long DAY_30_EXPIRE_TIME = 60 * 60 * 24 * 30L;

    public static final Long DAY_5_EXPIRE_TIME = 60 * 60 * 24 * 5L;

    // Redis里存所有活跃会话ID的Set key，方便查找所有会话
    public static final String JANE_CONVERSATION_YOUKE_KEY = "Jane_chat:ids_youke";

    public static final String JANE_CONVERSATION_USER_KEY = "Jane_chat:ids_user-";

    // 每个会话消息列表的key前缀
    public static final String JANE_MESSAGE_LIST_YOUKE_PREFIX = "Jane_chat:messages_youke";

    public static final String JANE_MESSAGE_LIST_USER_PREFIX = "Jane_chat:messages_user-";

    // 每个会话的title的key前缀 string
    public static final String JANE_CONVERSATION_TITLE_YOUKE_PREFIX = "Jane_chat:title_youke";
    public static final String JANE_CONVERSATION_TITLE_USER_PREFIX = "Jane_chat:title_user-";

    // 每个用户登录后的标识 string
    public static final String JANE_USER_HAS_LOGIN = "Jane_chat:user_login-";

    //游客会话次数
    public static final String JANE_YOUKE_TALK_NUM = "Jane_chat:youke_talk_nums:";

    public static final String JANE_CHAT_GAP_TIME = "Jane_chat_gaptime:";

    public static final String JANE_PDF_GAP_TIME = "Jane_pdf_gaptime:";

    public static final String JANE_REGISTER_GAP_TIME = "Jane_register_gaptime:";

    public static final String JANE_PDF_FILE = "Jane_pdf:";

    public static final String JANE_RANKING = "Jane_ranking";
}
