package com.common.jane_ai.redis_chat_memory;

import com.common.jane_ai.redis_chat_memory.dialect.RedisChatMemoryRepositoryDialect;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

//自定义chatmemory
public class RedisChatMemoryRepository implements ChatMemoryRepository {

    private final RedisChatMemoryRepositoryDialect dialect;

    public RedisChatMemoryRepository(RedisChatMemoryRepositoryDialect dialect) {
        this.dialect = dialect;
    }

    /**
     * 查询所有的对话ID列表。
     *
     * @return 返回所有存在的对话ID集合。
     */
    @Override
    public List<String> findConversationIds() {
        return dialect.findConversationIds();
    }

    /**
     * 根据对话ID查询该对话下的所有消息。
     *
     * @param conversationId 对话的唯一标识ID。
     * @return 返回该对话对应的消息列表。
     */
    @Override
    public List<Message> findByConversationId(String conversationId) {
        return dialect.findByConversationId(conversationId);
    }

    /**
     * 保存指定对话ID对应的消息列表，支持批量保存。
     *
     * @param conversationId 对话的唯一标识ID。
     * @param messages       需要保存的消息列表。
     */
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        dialect.saveAll(conversationId, messages);
    }

    /**
     * 删除指定对话ID对应的所有消息。
     *
     * @param conversationId 需要删除的对话ID。
     */
    @Override
    public void deleteByConversationId(String conversationId) {
        dialect.deleteByConversationId(conversationId);
    }
}
