package com.videoai.ai.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AsrResult {

    private String language;

    private Long durationMs;

    private String transcriptText;

    private List<AsrSegment> segments;
}
