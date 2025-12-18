package com.common.jane_ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.common.jane_ai.Obj.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
