package com.videoai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("ai_usage_record")
@EqualsAndHashCode(callSuper = true)
public class AiUsageRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long taskId;

    private String bizType;

    private String requestHash;

    private String modelName;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private Long durationMs;

    private Integer hitCache;

    private Integer success;

    private String errorMessage;
}
