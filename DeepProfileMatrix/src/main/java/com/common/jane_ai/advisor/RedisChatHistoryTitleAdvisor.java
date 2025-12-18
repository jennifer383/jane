package com.common.jane_ai.advisor;

import com.alibaba.fastjson.JSON;
import com.common.jane_ai.Obj.dto.ChatHistoryInfoDTO;
import com.common.jane_ai.Obj.entity.ChatHistory;
import com.common.jane_ai.constant.JaneStatusConstant;
import com.common.jane_ai.constant.JaneTypeConstant;
import com.common.jane_ai.service.RedisChatHistoryAdvisorService;
import com.common.jane_ai.threadlocal.BaseParameterHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.time.LocalDateTime;
import java.util.List;

import static com.common.jane_ai.constant.JaneDescConstant.JANE_TITLE;
import static com.common.jane_ai.constant.JaneTypeConstant.*;

@Slf4j
//会话历史记录Title
public class RedisChatHistoryTitleAdvisor implements BaseChatMemoryAdvisor {

    private final Integer order;

    private final RedisChatHistoryAdvisorService redisChatHistoryAdvisorService;

    private final ChatClient chatClient;

    private final String defaultConversationId;

    private final ChatMemory chatMemory;
    //是否需要实现 adviseStream()，取决于你是否使用了流式（streaming）响应模式。这是关键点：

    private RedisChatHistoryTitleAdvisor (Integer order, RedisChatHistoryAdvisorService redisChatHistoryAdvisorService, ChatClient chatClient, String defaultConversationId, ChatMemory chatMemory) {
        this.order = order;
        this.redisChatHistoryAdvisorService = redisChatHistoryAdvisorService;
        this.chatClient = chatClient;
        this.defaultConversationId = defaultConversationId;
        this.chatMemory = chatMemory;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        log.info("777after"+Thread.currentThread().getName());
        String conversationId = getConversationId(chatClientResponse.context(), this.defaultConversationId);
        Integer type = Integer.valueOf(JANE_TYPE_USER);
            log.info("SEC-2-userid-"+BaseParameterHolder.getBaseParameter(USER_ID));
        String userId = chatClientResponse.context().get(USER_ID).toString();

        //这里会切换线程
        BaseParameterHolder.setBaseParameter(JaneTypeConstant.USER_ID,userId);

        log.info("SEC-2-"+Thread.currentThread().getName());

        if(userId.equals(JaneTypeConstant.JANE_TYPE_YOUKE)){
            type = Integer.valueOf(JANE_TYPE_YOUKE);
        }
        //查看是否存在标题
        ChatHistory chatHistory = redisChatHistoryAdvisorService.getChatTypeHistory(type, conversationId);

        if(chatHistory.getTitle()!=null){
            return chatClientResponse;
        }
        List<Message> messages = chatMemory.get(conversationId);
        List<ChatHistoryInfoDTO> list = messages.stream().map(message -> new ChatHistoryInfoDTO(message)).toList();
        log.info("SEC-2-1-"+Thread.currentThread().getName());
        String title = chatClient.prompt().user(JANE_TITLE + JSON.toJSONString(list)).call().content();
        log.info("生成的标题: {}-{}", title, conversationId);
        chatHistory = new ChatHistory(type, conversationId, title, JaneStatusConstant.JANE_STATUS_AVILABLE, LocalDateTime.now(), LocalDateTime.now());
        redisChatHistoryAdvisorService.updateById(chatHistory);
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public static Builder builder(RedisChatHistoryAdvisorService redisChatHistoryAdvisorService){
        return new Builder(redisChatHistoryAdvisorService);
    }

    public static final class Builder {
        private Integer order=100;
        private RedisChatHistoryAdvisorService redisChatHistoryAdvisorService;
        private ChatClient chatClient;
        private ChatMemory chatMemory;

        public Builder(RedisChatHistoryAdvisorService redisChatHistoryAdvisorService) {
            this.redisChatHistoryAdvisorService = redisChatHistoryAdvisorService;
        }

        public RedisChatHistoryTitleAdvisor.Builder order(Integer order) {
            this.order = order;
            return this;
        }

        public RedisChatHistoryTitleAdvisor.Builder chatClient(ChatClient chatClient) {
            this.chatClient = chatClient;
            return this;
        }

        public RedisChatHistoryTitleAdvisor.Builder chatMemory(ChatMemory chatMemory) {
            this.chatMemory = chatMemory;
            return this;
        }

        public RedisChatHistoryTitleAdvisor build() {
            String conversationId = ChatMemory.DEFAULT_CONVERSATION_ID;
            return new RedisChatHistoryTitleAdvisor(order, redisChatHistoryAdvisorService, chatClient, conversationId, chatMemory);
        }

    }
}
