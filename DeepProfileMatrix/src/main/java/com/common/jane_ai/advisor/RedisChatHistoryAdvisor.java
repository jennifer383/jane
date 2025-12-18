package com.common.jane_ai.advisor;

import com.common.jane_ai.constant.JaneTypeConstant;
import com.common.jane_ai.service.RedisChatHistoryAdvisorService;
import com.common.jane_ai.threadlocal.BaseParameterHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.data.mapping.model.PreferredConstructorDiscoverer;
import org.springframework.util.StringUtils;

import java.util.Objects;

import static com.common.jane_ai.constant.JaneTypeConstant.*;

//会话历史记录
@Slf4j
public class RedisChatHistoryAdvisor implements BaseChatMemoryAdvisor {

    private final Integer order;

    private final String defaultConversationId;

    private final RedisChatHistoryAdvisorService redisChatHistoryAdvisorService;

    private RedisChatHistoryAdvisor(Integer order, String defaultConversationId, RedisChatHistoryAdvisorService redisChatHistoryAdvisorService){
        if(Objects.isNull(order)){
            throw new IllegalArgumentException("order cannot be null");
        }
        if(StringUtils.isEmpty(defaultConversationId)){
            throw new IllegalArgumentException("defaultConversationId cannot be empty");
        }
        if(Objects.isNull(redisChatHistoryAdvisorService)){
            throw new IllegalArgumentException("redisChatHistoryAdvisorService cannot be null");
        }

        this.order = order;
        this.defaultConversationId = defaultConversationId;
        this.redisChatHistoryAdvisorService = redisChatHistoryAdvisorService;
    }

    public static Builder builder(RedisChatHistoryAdvisorService redisChatHistoryAdvisorService) {
        return new Builder(redisChatHistoryAdvisorService);
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String conversationId = getConversationId(chatClientRequest.context(), this.defaultConversationId);
        String type = JaneTypeConstant.JANE_TYPE_USER;

        String userId = chatClientRequest.context().get(USER_ID).toString();
        BaseParameterHolder.setBaseParameter(JaneTypeConstant.USER_ID,userId);
        log.info("First-1-"+Thread.currentThread().getName());
        if(userId.equals(JaneTypeConstant.JANE_TYPE_YOUKE)){
            type = JANE_TYPE_YOUKE;
        }
        redisChatHistoryAdvisorService.save(type, conversationId);
        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public static class Builder{

        private Integer order = 99;
        private String defaultConversationId;
        private RedisChatHistoryAdvisorService redisChatHistoryAdvisorService;

        private Builder(RedisChatHistoryAdvisorService redisChatHistoryAdvisorService){
            this.redisChatHistoryAdvisorService = redisChatHistoryAdvisorService;
        }

        public RedisChatHistoryAdvisor.Builder order(Integer order) {
            this.order = order;
            return this;
        }

        public RedisChatHistoryAdvisor.Builder redisChatHistoryAdvisorService(RedisChatHistoryAdvisorService redisChatHistoryAdvisorService) {
            this.redisChatHistoryAdvisorService=redisChatHistoryAdvisorService;
            return this;
        }

        public RedisChatHistoryAdvisor build() {
            defaultConversationId = ChatMemory.DEFAULT_CONVERSATION_ID;
            return new RedisChatHistoryAdvisor(this.order,defaultConversationId,this.redisChatHistoryAdvisorService);
        }


    }
}
