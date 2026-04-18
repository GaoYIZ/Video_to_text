package com.videoai.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VideoTaskVO {

    private Long id;

    private String taskNo;

    private String videoTitle;

    private Integer status;

    private String statusCode;

    private String currentStep;

    private Integer progressPercent;

    private String failReason;

    private Long fileId;

    private LocalDateTime createTime;

    private LocalDateTime finishedAt;
}
