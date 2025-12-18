package com.common.jane_ai.Obj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.jane_ai.Obj.entity.base.BaseTableData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_data")
public class ChatHistory extends BaseTableData {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer type;

    private String chatId;

    private String title;

    private Boolean status;

    public ChatHistory(Integer type, String chatId,String title, Boolean status, LocalDateTime createTime, LocalDateTime updateTime) {
        this.type = type;
        this.chatId = chatId;
        this.title = title;
        this.status = status;
        this.setCreateTime(createTime);
        this.setUpdateTime(updateTime);
    }


}
