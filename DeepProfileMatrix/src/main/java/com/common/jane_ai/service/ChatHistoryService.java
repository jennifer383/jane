package com.common.jane_ai.service;

import com.common.jane_ai.Obj.dto.ChatHistoryDTO;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

public interface ChatHistoryService {
    List<ChatHistoryDTO> getTitleHistory();

    List<Message> getChatHistory(String chatId);
}
