package com.videoai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("video_task")
@EqualsAndHashCode(callSuper = true)
public class VideoTask extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskNo;

    private Long userId;

    private Long fileId;

    private String fileMd5;

    private String videoTitle;

    private Integer status;

    private String currentStep;

    private Integer progressPercent;

    private Integer retryCount;

    private String failReason;

    private String sessionId;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private Long costTimeMs;
}
