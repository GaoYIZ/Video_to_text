package com.videoai.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WorkbenchAiActionVO {

    private String action;

    private Long taskId;

    private String sessionId;

    private String answer;

    private String summary;

    private List<String> highlights;

    private List<TranscriptSegmentVO> citedSegments;
}
