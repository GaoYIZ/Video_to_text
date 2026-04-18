package com.videoai.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskStatusVO {

    private Long taskId;

    private Integer status;

    private String statusCode;

    private String currentStep;

    private Integer progressPercent;

    private String failReason;
}
