package com.videoai.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VideoTaskDetailVO {

    private Long id;

    private String taskNo;

    private String videoTitle;

    private Integer status;

    private String statusCode;

    private String currentStep;

    private Integer progressPercent;

    private String failReason;

    private Long fileId;

    private String fileMd5;

    private LocalDateTime createTime;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private Long costTimeMs;

    private List<TaskEventVO> events;
}
