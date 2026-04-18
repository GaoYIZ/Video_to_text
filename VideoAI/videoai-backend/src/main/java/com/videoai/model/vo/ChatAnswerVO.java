package com.videoai.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatAnswerVO {

    private String sessionId;

    private String answer;

    private List<TranscriptSegmentVO> citedSegments;

    private Boolean fromCache;
}
