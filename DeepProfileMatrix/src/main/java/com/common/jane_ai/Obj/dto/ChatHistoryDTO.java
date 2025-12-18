package com.common.jane_ai.Obj.dto;

import lombok.Data;

@Data
public class ChatHistoryDTO {

    private Long id;

    private Integer type;

    private String chatId;

    private String title;

    private Boolean status;
}
