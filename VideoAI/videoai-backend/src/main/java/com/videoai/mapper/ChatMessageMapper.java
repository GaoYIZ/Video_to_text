package com.videoai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.videoai.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
