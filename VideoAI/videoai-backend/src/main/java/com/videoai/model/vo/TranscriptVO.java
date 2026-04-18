package com.videoai.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TranscriptVO {

    private Long transcriptId;

    private String language;

    private Long durationMs;

    private Integer wordCount;

    private String transcriptText;

    private List<TranscriptSegmentVO> segments;
}
