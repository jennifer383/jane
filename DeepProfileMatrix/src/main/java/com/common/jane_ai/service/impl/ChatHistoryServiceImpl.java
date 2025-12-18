package com.common.jane_ai.service.impl;

import com.alibaba.fastjson.JSON;
import com.common.jane_ai.Obj.dto.ChatHistoryDTO;
import com.common.jane_ai.lua.ChatHistoryTitle;
import com.common.jane_ai.redis_chat_memory.dialect.RedisChatMemoryRepositoryDialect;
import com.common.jane_ai.service.ChatHistoryService;
import com.common.jane_ai.threadlocal.BaseParameterHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.common.jane_ai.constant.JaneRedisKeyConstant.JANE_CONVERSATION_TITLE_USER_PREFIX;
import static com.common.jane_ai.constant.JaneTypeConstant.USER_ID;

@Service
@Slf4j
public class ChatHistoryServiceImpl implements ChatHistoryService {

    @Autowired
    private RedisChatMemoryRepositoryDialect dialect;
    @Autowired
    public RedisTemplate<String, String> redisTemplate;
    @Autowired
    private ChatHistoryTitle chatHistoryTitle;

    @Override
    public List<ChatHistoryDTO> getTitleHistory() {
        String userId = BaseParameterHolder.getBaseParameter(USER_ID);
        List<String> ids = dialect.findConversationIds();
        String keypre = JANE_CONVERSATION_TITLE_USER_PREFIX+userId+":";
        //lua脚本获取，减少网络交互
        List<ChatHistoryDTO> chatHistoryDTOS = chatHistoryTitle.chathostoryTitleOperate(List.of(keypre), ids.toArray(new String[0]));
        log.info("获取会话历史记录成功"+userId);
        return chatHistoryDTOS;
    }

    @Override
    public List<Message> getChatHistory(String chatId) {
        String userId = BaseParameterHolder.getBaseParameter(USER_ID);
        List<Message> messages = dialect.findByConversationId(chatId);
        log.info("获取会话历史记录成功"+userId);
        return messages;
    }
}
