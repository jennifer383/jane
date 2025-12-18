package com.common.jane_ai.controller;

import com.alibaba.fastjson.JSON;
import com.common.jane_ai.Obj.dto.ChatHistoryDTO;
import com.common.jane_ai.Obj.dto.chathistory;
import com.common.jane_ai.common.ApiResponse;
import com.common.jane_ai.redis_chat_memory.dialect.RedisChatMemoryRepositoryDialect;
import com.common.jane_ai.service.ChatHistoryService;
import com.common.jane_ai.service.RedisChatHistoryAdvisorService;
import com.common.jane_ai.threadlocal.BaseParameterHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.common.jane_ai.constant.JaneRedisKeyConstant.JANE_CONVERSATION_TITLE_USER_PREFIX;
import static com.common.jane_ai.constant.JaneRedisKeyConstant.JANE_CONVERSATION_USER_KEY;
import static com.common.jane_ai.constant.JaneTypeConstant.USER_ID;

@RestController
@RequestMapping("/jane")
@Slf4j
public class ChatHistoryController {

    @Autowired
    private RedisChatHistoryAdvisorService redisChatHistoryAdvisorService;
    @Autowired
    private ChatHistoryService chatHistoryService;
    //与object获取对象的不同

    @PostMapping("/titlehistory")
    //可以把这个放在lua脚本结合本地缓存
    public ApiResponse<List<ChatHistoryDTO>> getTitleHistory(){
        return ApiResponse.ok(chatHistoryService.getTitleHistory());
    }

    @GetMapping("/chathistory")
    //这个可以直接调用
    public ApiResponse<List<Message>> getChatHistory(@RequestParam String chatId){
        return ApiResponse.ok(chatHistoryService.getChatHistory(chatId));
    }

}
