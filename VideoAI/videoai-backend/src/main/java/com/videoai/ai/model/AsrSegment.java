package com.videoai.ai.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AsrSegment {

    private Integer index;

    private Long startTimeMs;

    private Long endTimeMs;

    private String content;
}
