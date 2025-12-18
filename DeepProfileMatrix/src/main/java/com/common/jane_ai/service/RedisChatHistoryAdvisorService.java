package com.common.jane_ai.service;

import com.common.jane_ai.Obj.entity.ChatHistory;
import com.common.jane_ai.Obj.dto.ChatHistoryDTO;

import java.util.List;

//会话历史的操作接口
public interface RedisChatHistoryAdvisorService {

    /**
     * 保存会话记录
     * @param type 业务类型
     * @param chatId 会话ID
     */
    void save(String type, String chatId);

    /**
     * 获取会话ID列表
     * @param type 业务类型
     * @return 会话ID列表
     */
    List<String> getChatIdList(Integer type);

    /**
     * 删除会话记录
     * @param type 业务类型
     * @param chatId 会话ID
     */
    void delete(Integer type, String chatId);

    /**
     * 获取会话
     * @param type 业务类型
     * @param chatId 会话ID
     * @return 会话
     */
    ChatHistory getChatTypeHistory(Integer type, String chatId);

    /**
     * 更新会话类型历史
     * @param chatHistory 会话类型历史记录
     */
    void updateById(ChatHistory chatHistory);

    /**
     * 获取会话列表
     * @param type 业务类型
     * @return 会话列表
     */
    List<ChatHistoryDTO> getChatTypeHistoryList(Integer type);

}
