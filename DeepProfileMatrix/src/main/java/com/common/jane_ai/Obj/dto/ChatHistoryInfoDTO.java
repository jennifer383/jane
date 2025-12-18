package com.common.jane_ai.Obj.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryInfoDTO {

    public String role;

    public String content;

    public ChatHistoryInfoDTO(Message  message){
        this.role = message.getMessageType().toString();
        this.content = message.getText();
    }
}
