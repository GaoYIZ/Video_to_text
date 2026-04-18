package com.videoai.ai.model;

import com.videoai.model.vo.TranscriptSegmentVO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuestionAnswerResult {

    private String answer;

    private List<TranscriptSegmentVO> citedSegments;

    private String modelName;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private Long durationMs;

    private boolean cacheHit;
}
