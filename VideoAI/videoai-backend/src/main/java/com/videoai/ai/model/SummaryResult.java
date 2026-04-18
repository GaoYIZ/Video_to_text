package com.videoai.ai.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SummaryResult {

    private String title;

    private String summary;

    private List<String> outline;

    private List<String> keywords;

    private List<String> highlights;

    private List<String> qaSuggestions;

    private String modelName;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private Long durationMs;
}
