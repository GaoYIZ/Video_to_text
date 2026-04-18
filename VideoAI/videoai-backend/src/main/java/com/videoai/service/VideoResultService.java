package com.videoai.service;

import com.videoai.model.vo.SummaryVO;
import com.videoai.model.vo.TranscriptSegmentVO;
import com.videoai.model.vo.TranscriptVO;

import java.util.List;

public interface VideoResultService {

    TranscriptVO getTranscript(Long userId, Long taskId);

    SummaryVO getSummary(Long userId, Long taskId);

    List<TranscriptSegmentVO> getSegments(Long userId, Long taskId);
}
