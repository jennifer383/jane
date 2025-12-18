package com.common.jane_ai.lua;

import com.alibaba.fastjson.JSON;
import com.common.jane_ai.Obj.dto.ChatHistoryDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//查询历史会话标题的lua
@Component
@Slf4j
public class ChatHistoryTitle {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private DefaultRedisScript defaultRedisScript;

    @PostConstruct
    public void init() {
        try {
            defaultRedisScript = new DefaultRedisScript<>();
            defaultRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/chathistorytitle.lua")));
            defaultRedisScript.setResultType(List.class);
        } catch (Exception e) {
            log.info("初始化lua脚本失败-ChatHistoryTitle");
        }
    }

    /**
     * 执行lua脚本，返回查询的字符串数组结果
     * @param keys 传入的key列表
     * @param args 传入的参数（如果lua脚本中有ARGV）
     * @return 查询结果字符串列表
     */
    public List<ChatHistoryDTO> chathostoryTitleOperate(List<String> keys, String[] args) {
        Object o = redisTemplate.execute(defaultRedisScript, keys, args);
        List<ChatHistoryDTO> chatHistoryDTOs = new ArrayList<>();
        for(String s: (List<String>)o){
            ChatHistoryDTO dto = JSON.parseObject(s, ChatHistoryDTO.class);
            chatHistoryDTOs.add(dto);
        }
        return chatHistoryDTOs;
    }












}
