package com.videoai.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskEventVO {

    private String fromStatus;

    private String toStatus;

    private String step;

    private String eventType;

    private Integer success;

    private String detail;

    private LocalDateTime createTime;
}
