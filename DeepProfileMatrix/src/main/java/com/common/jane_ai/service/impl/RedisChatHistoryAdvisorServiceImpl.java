package com.common.jane_ai.service.impl;

import com.alibaba.fastjson.JSON;
import com.common.jane_ai.Obj.entity.ChatHistory;
import com.common.jane_ai.Obj.dto.ChatHistoryDTO;
import com.common.jane_ai.constant.JaneTypeConstant;
import com.common.jane_ai.service.RedisChatHistoryAdvisorService;
import com.common.jane_ai.threadlocal.BaseParameterHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.common.jane_ai.constant.JaneRedisKeyConstant.*;
import static com.common.jane_ai.constant.JaneTypeConstant.JANE_TYPE_YOUKE;
import static com.common.jane_ai.constant.JaneTypeConstant.USER_ID;

@Service
@Slf4j
public class RedisChatHistoryAdvisorServiceImpl implements RedisChatHistoryAdvisorService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    /**
     * 返回不同类型的key值
     * @param YOUKEcommonKey
     * @param USERcommonKey
     * @return
     */
    public String getTypeKey(String YOUKEcommonKey,String USERcommonKey){
        log.info("THREE-3-"+Thread.currentThread().getName());
        String userId = BaseParameterHolder.getBaseParameter(USER_ID);
        // 添加null检查，避免空指针异常
        if(userId == null || userId.equals(JANE_TYPE_YOUKE)){
            return YOUKEcommonKey;
        }
        return USERcommonKey+userId;
    }
    @Override
    public void save(String type, String chatId) {
        //先判断，尽量减少逻辑
        String key = getTypeKey(JANE_CONVERSATION_TITLE_YOUKE_PREFIX,JANE_CONVERSATION_TITLE_USER_PREFIX)+":"+chatId;
        if(redisTemplate.hasKey(key)){
            return;
        }
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setType(Integer.valueOf(type));
        chatHistory.setChatId(chatId);
        if(!redisTemplate.hasKey(key)){
            redisTemplate.opsForValue().set(key, JSON.toJSONString(chatHistory));
        }
    }

    @Override
    public List<String> getChatIdList(Integer type) {
        return List.of();
    }

    @Override
    public void delete(Integer type, String chatId) {
        String key = getTypeKey(JANE_CONVERSATION_TITLE_YOUKE_PREFIX,JANE_CONVERSATION_TITLE_USER_PREFIX)+":"+chatId;
        redisTemplate.delete(key);
    }

    @Override
    public ChatHistory getChatTypeHistory(Integer type, String chatId) {
        log.info("THREE-3-1-"+Thread.currentThread().getName());
        String key = getTypeKey(JANE_CONVERSATION_TITLE_YOUKE_PREFIX,JANE_CONVERSATION_TITLE_USER_PREFIX)+":"+chatId;
        String value =redisTemplate.opsForValue().get(key);
        // 添加null检查，避免空指针异常
        if (value == null) {
            return null;
        }
        return JSON.parseObject(value, ChatHistory.class);
    }

    @Override
    public void updateById(ChatHistory chatHistory) {
        chatHistory.setTitle(chatHistory.getTitle().replaceAll("(?s)<think>.*?</think>", "").trim());
        delete(chatHistory.getType(), chatHistory.getChatId());
        String key = getTypeKey(JANE_CONVERSATION_TITLE_YOUKE_PREFIX,JANE_CONVERSATION_TITLE_USER_PREFIX)+":"+ chatHistory.getChatId();
//        30天的过期时间  是原子的  SET key value EX seconds
        redisTemplate.opsForValue().set(key, JSON.toJSONString(chatHistory), DAY_30_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    @Override
    public List<ChatHistoryDTO> getChatTypeHistoryList(Integer type) {
        return List.of();
    }
}
