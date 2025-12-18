
package com.common.jane_ai.redis_chat_memory.dialect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.jane_ai.Obj.entity.ChatMessage;
import com.common.jane_ai.mapper.ChatMessageMapper;
import com.common.jane_ai.threadlocal.BaseParameterHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.objenesis.ObjenesisException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static com.common.jane_ai.constant.JaneRedisKeyConstant.*;
import static com.common.jane_ai.constant.JaneRedisKeyConstant.DAY_5_EXPIRE_TIME;
import static com.common.jane_ai.constant.JaneTypeConstant.*;

//redis执行语句方法
@Slf4j
@Component
public class RedisChatMemoryRepositoryDialect {

    //单例对象
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplateString;//在会话id集合中更美观

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;

    /**
     * 返回不同类型的key值
     * @param YOUKEcommonKey
     * @param USERcommonKey
     * @return
     */
    public String getTypeKey(String YOUKEcommonKey,String USERcommonKey){
        String userId = BaseParameterHolder.getBaseParameter(USER_ID);
        log.info("LAST-CONVERSITION-4-1-"+Thread.currentThread().getName());
        if(userId.equals(JANE_TYPE_YOUKE)){
            return YOUKEcommonKey;
        }
        return USERcommonKey+userId;
    }

    /**
     * 获取所有活跃会话ID
     * Redis数据结构：Set（无序且唯一）
     * 用于快速获取当前所有存在的会话ID
     */
    public List<String> findConversationIds() {
        log.info("会话记忆-findConversationIds-All");
        String key = getTypeKey(JANE_CONVERSATION_YOUKE_KEY,JANE_CONVERSATION_USER_KEY);
        Set<Object> members = redisTemplate.opsForSet().members(key);
        return Optional.ofNullable(members)
                .filter(m -> !m.isEmpty())
                .map(m -> m.stream().map(Object::toString).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public List<String> findUserAllChatIds(String userId){
        String key = JANE_CONVERSATION_USER_KEY+userId;
        Set<Object> members = redisTemplate.opsForSet().members(key);
        return Optional.ofNullable(members)
                .filter(m -> !m.isEmpty())
                .map(m -> m.stream().map(Object::toString).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    /**
     * 根据会话ID获取该会话的所有消息列表（多轮对话历史）反序列化
     * Redis数据结构：List（有序）
     * 按消息顺序返回，方便构造对话上下文
     */
    public List<Message> findByConversationId(String conversationId){
        log.info("999before" + Thread.currentThread().getName());
        log.info("会话记忆-findByConversationId-" + conversationId);
        String key = getTypeKey(JANE_MESSAGE_LIST_YOUKE_PREFIX, JANE_MESSAGE_LIST_USER_PREFIX)+ ":" + conversationId;
        Long size = redisTemplate.opsForList().size(key);

        //触发查询请求的时候有可能为DB中也有可能在redis,先查询一次db，如果没有就直接查redis，有的话就异步把db同步到redis
        //先查缓存，没有再查数据库
        long start = Math.max(0, size - 21);
        List<Object> range = redisTemplate.opsForList().range(key, start, -1);
        //缓存中没有
        if(range == null || range.size() == 0){
            //从数据库中查找详情  会自动在之后调用saveAll方法把数据同步到redis
            range = searchFromPg(conversationId);
            if(range == null || range.size() == 0){
                return Collections.emptyList();
            }
        }//有几条返回几条，不是固定数量。

        List<Message> messages = new ArrayList<>();
        for(Object o:range){
            String json = JSON.toJSONString(o);
            try {       // 从 JsonParser 中读取 JSON 数据，并将其反序列化为 JsonNode（树形结构）对象
                JsonNode jsonNode = objectMapper.readTree(json);
                messages.add(getMessage(jsonNode));
//                log.info("999 " + getMessage(jsonNode).getMessageType());
            } catch(JsonProcessingException e){
                throw new RuntimeException("Error deserializing message", e);
            }
        }
//        List<Message> messages = range.stream().map(n -> JSON.parseObject(JSON.toJSONString(n), Message.class)).toList();
        return messages;
    }

    public List<Object> searchFromPg(String conversationId){
        log.info("会话记忆-searchFromPg-" + conversationId);
        LambdaQueryWrapper<ChatMessage> queryWrapper = Wrappers.lambdaQuery(ChatMessage.class)
                .eq(ChatMessage::getChatId, conversationId);
        List<ChatMessage> chatMessages = chatMessageMapper.selectList(queryWrapper);
        return new ArrayList<>(chatMessages);
    }

    //✅ 总结
    //方式	优点	缺点
    //readTree() + 手动构建 ✅	灵活处理结构、动态决定子类、字段默认值控制	稍多代码
    //直接反序列化 ❌	代码简洁	无法处理多态、字段控制差、易失败

    //为什么不直接使用json?
    //使用 JsonNode 主要是为了 灵活处理动态结构的 JSON 数据。它适合：
    //多种消息类型（多态）
    //字段可能缺失，需默认处理；
    //想对字段做自定义增强（如 timestamp）；
    //或 JSON 结构不稳定 / 可能变动；
    //所以你现在的方式 readTree() → 自定义构造 Message 对象，更安全、更灵活、更可维护，是非常合理的设计选择。
    /**
     * 将一个 JsonNode 转换成对应的 Message 子类实例。
     * 根据 messageType 字段决定返回哪种 Message 类型，并提取 text 和 metadata 字段。
     * 额外会在 metadata 中添加当前时间戳。
     *
     * @param jsonNode 传入的 JSON 树节点，包含 messageType、text、metadata 等字段
     * @return 对应类型的 Message 对象实例（AssistantMessage、UserMessage、SystemMessage 或 ToolResponseMessage）
     */
    private Message getMessage(JsonNode jsonNode) {
        // 从 jsonNode 中获取 messageType 字段的文本内容，默认为 USER 类型
        String type = Optional.ofNullable(jsonNode)
                .map(node -> node.get("messageType"))  // 取 messageType 字段节点
                .map(JsonNode::asText) //JsonNode::asText 是方法引用，等价于 node -> node.asText() // 转为字符串
                .orElse(MessageType.USER.getValue()); // 如果没有该字段，默认是 USER 类型

        // 根据字符串转换为枚举类型 MessageType
        MessageType messageType = MessageType.valueOf(type.toUpperCase());
        //根据大写的字符串名称查找对应的MessageType枚举常量

        // 从 jsonNode 中获取 text 字段的内容
        String textContent = Optional.ofNullable(jsonNode)
                .map(node -> node.get("text"))    // 取 text 字段节点
                .map(JsonNode::asText)            // 转为字符串
                // 如果 text 字段不存在，根据消息类型返回默认值：
                // SYSTEM 和 USER 类型默认返回空字符串 ""，其他类型返回 null
                .orElseGet(() ->
                        (messageType == MessageType.SYSTEM || messageType == MessageType.USER)
                                ? "" : null);

        // 从 jsonNode 中获取 metadata 字段并转换为 Map<String, Object>
        Map<String, Object> metadata = Optional.ofNullable(jsonNode)
                .map(node -> node.get("metadata"))       // 取 metadata 节点
                .map(node -> objectMapper.convertValue( // 用 Jackson ObjectMapper 转换成 Map
                        node, new TypeReference<Map<String, Object>>() {}))
                .orElse(new HashMap<>());                 // 如果没有 metadata 字段，返回空 Map

        // 在 metadata 中加入当前时间戳，key 是 "timestamp"，值是当前 ISO 格式时间字符串
        if(!metadata.containsKey("timestamp")){
            metadata.put("timestamp", Instant.now().atZone(ZoneId.of("Asia/Shanghai")).toString());
        }
        if(jsonNode.get("timestamp") != null){
            metadata.put("timestamp", jsonNode.get("timestamp").asText());
        }
        //Instant: 代表UTC时间轴上的一个瞬时点，无时区概念
        //LocalDateTime: 代表本地日期时间，无时区信息

        // 根据不同的消息类型，构造对应的 Message 子类实例并返回
        return switch (messageType) {
            case ASSISTANT -> new AssistantMessage(textContent, metadata);          // 助手消息
            case USER -> UserMessage.builder().text(textContent).metadata(metadata).build();   // 用户消息
            case SYSTEM -> SystemMessage.builder().text(textContent).metadata(metadata).build(); // 系统消息
            case TOOL -> new ToolResponseMessage(List.of(), metadata);                // 工具调用消息
        };
    }

    /**
     * 在saveall操作时统一添加系统时间
     * @param jsonNode
     * @param messageType
     * @param textContent
     * @return
     */
    private Message getMessageWithTime(JsonNode jsonNode,MessageType messageType,String textContent){

        // 清除 <think>...</think> 标签及其内容
        //(?s)：开启 DOTALL 模式，使 . 匹配包括换行符在内的所有字符；     正则
        //<think>.*?</think>：非贪婪匹配 <think> 和 </think> 之间的所有内容，包括多行。
        textContent  = textContent.replaceAll("(?s)<think>.*?</think>", "").trim();

        // 从 jsonNode 中获取 metadata 字段并转换为 Map<String, Object>
        Map<String, Object> metadata = Optional.ofNullable(jsonNode)
                .map(node -> node.get("metadata"))       // 取 metadata 节点
                .map(node -> objectMapper.convertValue( // 用 Jackson ObjectMapper 转换成 Map
                        node, new TypeReference<Map<String, Object>>() {}))
                .orElse(new HashMap<>());                 // 如果没有 metadata 字段，返回空 Map

        // 在 metadata 中加入当前时间戳，key 是 "timestamp"，值是当前 ISO 格式时间字符串
        if(!metadata.containsKey("timestamp")){
            metadata.put("timestamp", Instant.now().atZone(ZoneId.of("Asia/Shanghai")).toString());
        }

        // 根据不同的消息类型，构造对应的 Message 子类实例并返回
        return switch (messageType) {
            case ASSISTANT -> new AssistantMessage(textContent, metadata);          // 助手消息
            case USER -> UserMessage.builder().text(textContent).metadata(metadata).build();   // 用户消息
            case SYSTEM -> SystemMessage.builder().text(textContent).metadata(metadata).build(); // 系统消息
            case TOOL -> new ToolResponseMessage(List.of(), metadata);                // 工具调用消息
        };
    }

    /**
     * 保存一批消息到指定会话中，追加到消息列表末尾
     * Redis数据结构：List（右侧追加）
     * 并且保证会话ID存在于会话ID集合中
     */
    public void saveAll(String conversationId, List<Message> messages) {
        log.info("999before"+Thread.currentThread().getName());
        log.info("会话记忆-saveAll-"+conversationId);
//        if(messages == null || messages.isEmpty()) return;
        if(CollectionUtils.isEmpty(messages)) return;
        String listkey = getTypeKey(JANE_MESSAGE_LIST_YOUKE_PREFIX,JANE_MESSAGE_LIST_USER_PREFIX)+":"+conversationId;
        String setkey = getTypeKey(JANE_CONVERSATION_YOUKE_KEY,JANE_CONVERSATION_USER_KEY);
        //防止重复加载//TODO:判断是否内部重复
        deleteByConversationId(conversationId);
        // 将会话ID添加到Set，保证可以查询到这个会话
        redisTemplateString.opsForSet().add(setkey, conversationId);
        if(BaseParameterHolder.getBaseParameter(USER_ID) == JANE_TYPE_YOUKE){
            redisTemplateString.expire(setkey,DAY_5_EXPIRE_TIME, TimeUnit.SECONDS);
        }else{
            redisTemplateString.expire(setkey,DAY_30_EXPIRE_TIME, TimeUnit.SECONDS);
        }
        List<Message> filteredMessages = messages.stream()
                .filter(Objects::nonNull)
                .filter(m -> m.getText() != null && m.getMessageType() != null).toList();
        List<Message> finalMessages = new ArrayList<>();
        for(Message message:filteredMessages){
            String json = JSON.toJSONString(message);
            try {
                JsonNode jsonNode = objectMapper.readTree(json);                //包括think
                finalMessages.add(getMessageWithTime(jsonNode,message.getMessageType(),message.getText()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        redisTemplate.opsForList().rightPushAll(listkey, finalMessages.toArray());
        redisTemplate.expire(listkey, DAY_30_EXPIRE_TIME, TimeUnit.SECONDS);
        // 可选：控制消息列表最大长度，防止无限增大
        int maxHistorySize = 20;//这里的条数理应和ai配置文件一致，因为内部调用接口会删除，因为删除的是会话ID
        //会保留索引从5到34的元素（共30个元素），删除索引0到4的元素。
        //所以答案是：是的，包括前后下标，两端都是包含的
        redisTemplate.opsForList().trim(listkey, -maxHistorySize, -1);
    }


    /**
     * 删除指定会话的所有消息以及会话ID集合中的对应ID
     * Redis数据结构：删除List + Set中元素
     */
    public void deleteByConversationId(String conversationId) {
        log.info("9992mid"+Thread.currentThread().getName());
        log.info("会话记忆-deleteByConversationId-"+conversationId);
        String listkey = getTypeKey(JANE_MESSAGE_LIST_YOUKE_PREFIX,JANE_MESSAGE_LIST_USER_PREFIX)+":"+conversationId;
        String setkey = getTypeKey(JANE_CONVERSATION_YOUKE_KEY,JANE_CONVERSATION_USER_KEY);
        redisTemplate.delete(listkey);
        redisTemplate.opsForSet().remove(setkey, conversationId);
    }

//    主要问题：单例模式和Spring依赖注入冲突
//    你用 @Autowired 注入了 redisTemplate，这是 Spring 的依赖注入。
//    你又写了一个私有构造器 + 双重检查锁的单例 getInstance() 方法，手动管理单例实例。
//    这样做冲突了：Spring管理Bean生命周期和依赖注入，你用单例模式自己创建对象，导致 redisTemplate 无法注入（因为你用的是 new 出来的实例，不是Spring管理的Bean）
//    private volatile static RedisChatMemoryRepositoryDialect instance;
//
//    private RedisChatMemoryRepositoryDialect() {
//    }
//
//    public static RedisChatMemoryRepositoryDialect getInstance() {
//        if (instance == null) {
//            synchronized (RedisChatMemoryRepositoryDialect.class) {
//                if (instance == null) {
//                    instance = new RedisChatMemoryRepositoryDialect();
//                }
//            }
//        }
//        return instance;
//    }
//    // 方法
}
