package com.videoai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("chat_message")
@EqualsAndHashCode(callSuper = true)
public class ChatMessage extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long userId;

    private String sessionId;

    private String role;

    private String messageType;

    private String content;

    private String citedSegmentsJson;

    private String modelName;

    private Integer totalTokens;
}
