package com.videoai.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SummaryVO {

    private String title;

    private String summary;

    private List<String> outline;

    private List<String> keywords;

    private List<String> highlights;

    private List<String> qaSuggestions;

    private String modelName;

    private Integer totalTokens;

    private Long costTimeMs;
}
