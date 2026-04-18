package com.videoai.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TranscriptSegmentVO {

    private Long id;

    private Integer segmentIndex;

    private Long startTimeMs;

    private Long endTimeMs;

    private String content;

    private String keywords;
}
