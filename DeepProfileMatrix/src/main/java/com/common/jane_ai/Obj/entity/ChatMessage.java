package com.common.jane_ai.Obj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.jane_ai.Obj.entity.base.BaseTableData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("chat_messages")
public class ChatMessage extends BaseTableData {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String chatId;

    private String messageType;

    private String timestamp;

    private String text;

}
