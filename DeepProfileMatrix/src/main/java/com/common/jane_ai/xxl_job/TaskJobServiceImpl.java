package com.common.jane_ai.xxl_job;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.jane_ai.Obj.entity.ChatMessage;
import com.common.jane_ai.Obj.entity.User;
import com.common.jane_ai.enums.BaseCode;
import com.common.jane_ai.exception.JaneFrameworkException;
import com.common.jane_ai.mapper.ChatMessageMapper;
import com.common.jane_ai.mapper.UserMapper;
import com.common.jane_ai.redis_chat_memory.dialect.RedisChatMemoryRepositoryDialect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.common.jane_ai.constant.JaneRedisKeyConstant.JANE_MESSAGE_LIST_USER_PREFIX;

@Slf4j
@Component
public class TaskJobServiceImpl {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private RedisChatMemoryRepositoryDialect redisChatMemoryRepositoryDialect;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @XxlJob("test")
    public void test()throws Exception {
        XxlJobHelper.log("XXL-JOB, Hello World.");
        log.info("xxljob测试");
        // default success
    }

    @XxlJob("PersistentStorage")
    @Transactional
    public void PersistentStorage()throws Exception {
        XxlJobHelper.log("PersistentStorage");
        log.info("PersistentStorage");
        //对会话5天后的数据进行持久化存储并且删除redis
//     25天的秒数是 2,160,000 秒
        try {
            LambdaQueryWrapper<User> queryWrapper = Wrappers
                    .lambdaQuery(User.class)
                    .select(User::getId);
            List<Long> userIds = userMapper.selectList(queryWrapper).stream()
                    .map(User::getId).toList();
            for(Long userId : userIds){//所有用户
                List<String> chatIds = redisChatMemoryRepositoryDialect.findUserAllChatIds(userId.toString());
                for(String chatId : chatIds){//一个用户的所有会话
                    String key = JANE_MESSAGE_LIST_USER_PREFIX+ userId +":" + chatId;
                        //获取一个会话的所有信息放在DB，没有就跳过
                    if(redisTemplate.getExpire(key)>2160000){
                        continue;
                    }
                    //会话中消息集合
                    List<Object> range = redisTemplate.opsForList().range(key, 0, -1);
                    if(range!= null && range.size()!=0){
                        List<ChatMessage> messages = new ArrayList<>();
                        for(Object o:range){
                            String json = JSON.toJSONString(o);
                            JsonNode jsonNode = objectMapper.readTree(json);
                            ChatMessage chatMessage = ChatMessage.builder()
                                    .messageType(jsonNode.get("messageType").asText())
                                    .text(jsonNode.get("text").asText())
                                    .timestamp(jsonNode.get("metadata").get("timestamp").asText())
                                    .chatId(chatId)
                                    .userId(userId)
                                    .build();
                            messages.add(chatMessage);
                        }
                        Set<String> existSet = chatMessageMapper.selectList(Wrappers.lambdaQuery(ChatMessage.class)
                                        .eq(ChatMessage::getChatId, chatId))
                                .stream()
                                .map(ChatMessage::getTimestamp)
                                .collect(Collectors.toSet());

                        //如果已经存在就跳过
                        List<BatchResult> list = chatMessageMapper.insertOrUpdate(messages, (BatchSqlSession, message) -> {
                            // // 这里可以添加去重逻辑的判断条件
                            //    // 如果返回true，则执行插入或更新操作
                            //    // 如果返回false，则跳过该条记录
                            return !existSet.contains(message.getTimestamp());
                        });
                        log.info("用户:"+userId+"会话:"+chatId+"DB-持久化成功");
                        //这下面的删除方法需要threadlocal中的userId// 并且需要删除set中的数据
                        String listKey = JANE_MESSAGE_LIST_USER_PREFIX + userId +":" + chatId;
                        if(list.isEmpty()){
                            throw new JaneFrameworkException(BaseCode.XXL_JOB_ERROR);
                        }
                        redisTemplate.delete(listKey);
//                        BaseParameterHolder.setBaseParameter(USER_ID,userId.toString());
//                        redisChatMemoryRepositoryDialect.deleteByConversationId(chatId);
                        log.info("用户:"+userId+"会话:"+chatId+"Redis-删除成功");
                    }
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        //对20条之后的数据进行持久化存储//麻烦
        // default success
    }

}
