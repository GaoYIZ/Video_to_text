package com.videoai.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.videoai.common.enums.ErrorCode;
import com.videoai.common.exception.BusinessException;
import com.videoai.common.util.JsonUtils;
import com.videoai.mapper.VideoSummaryMapper;
import com.videoai.mapper.VideoTaskMapper;
import com.videoai.mapper.VideoTranscriptMapper;
import com.videoai.mapper.VideoTranscriptSegmentMapper;
import com.videoai.model.entity.VideoSummary;
import com.videoai.model.entity.VideoTask;
import com.videoai.model.entity.VideoTranscript;
import com.videoai.model.entity.VideoTranscriptSegment;
import com.videoai.model.vo.SummaryVO;
import com.videoai.model.vo.TranscriptSegmentVO;
import com.videoai.model.vo.TranscriptVO;
import com.videoai.service.VideoResultService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoResultServiceImpl implements VideoResultService {

    private final VideoTaskMapper videoTaskMapper;
    private final VideoTranscriptMapper videoTranscriptMapper;
    private final VideoTranscriptSegmentMapper segmentMapper;
    private final VideoSummaryMapper videoSummaryMapper;

    public VideoResultServiceImpl(VideoTaskMapper videoTaskMapper, VideoTranscriptMapper videoTranscriptMapper,
                                  VideoTranscriptSegmentMapper segmentMapper, VideoSummaryMapper videoSummaryMapper) {
        this.videoTaskMapper = videoTaskMapper;
        this.videoTranscriptMapper = videoTranscriptMapper;
        this.segmentMapper = segmentMapper;
        this.videoSummaryMapper = videoSummaryMapper;
    }

    @Override
    public TranscriptVO getTranscript(Long userId, Long taskId) {
        VideoTask task = checkTask(taskId, userId);
        VideoTranscript transcript = videoTranscriptMapper.selectOne(
                Wrappers.<VideoTranscript>lambdaQuery().eq(VideoTranscript::getTaskId, task.getId()));
        if (transcript == null) {
            return null;
        }
        return TranscriptVO.builder()
                .transcriptId(transcript.getId())
                .language(transcript.getLanguage())
                .durationMs(transcript.getDurationMs())
                .wordCount(transcript.getWordCount())
                .transcriptText(transcript.getTranscriptText())
                .segments(getSegments(userId, taskId))
                .build();
    }

    @Override
    public SummaryVO getSummary(Long userId, Long taskId) {
        VideoTask task = checkTask(taskId, userId);
        VideoSummary summary = videoSummaryMapper.selectOne(
                Wrappers.<VideoSummary>lambdaQuery().eq(VideoSummary::getTaskId, task.getId()));
        if (summary == null) {
            return null;
        }
        return SummaryVO.builder()
                .title(summary.getTitle())
                .summary(summary.getSummary())
                .outline(JsonUtils.toStringList(summary.getOutlineJson()))
                .keywords(JsonUtils.toStringList(summary.getKeywordsJson()))
                .highlights(JsonUtils.toStringList(summary.getHighlightsJson()))
                .qaSuggestions(JsonUtils.toStringList(summary.getQaSuggestionsJson()))
                .modelName(summary.getModelName())
                .totalTokens(summary.getTotalTokens())
                .costTimeMs(summary.getCostTimeMs())
                .build();
    }

    @Override
    public List<TranscriptSegmentVO> getSegments(Long userId, Long taskId) {
        VideoTask task = checkTask(taskId, userId);
        List<VideoTranscriptSegment> segments = segmentMapper.selectList(
                Wrappers.<VideoTranscriptSegment>lambdaQuery()
                        .eq(VideoTranscriptSegment::getTaskId, task.getId())
                        .orderByAsc(VideoTranscriptSegment::getSegmentIndex));
        return segments.stream().map(segment -> TranscriptSegmentVO.builder()
                .id(segment.getId())
                .segmentIndex(segment.getSegmentIndex())
                .startTimeMs(segment.getStartTimeMs())
                .endTimeMs(segment.getEndTimeMs())
                .content(segment.getContent())
                .keywords(segment.getKeywords())
                .build()).toList();
    }

    private VideoTask checkTask(Long taskId, Long userId) {
        VideoTask task = videoTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }
        if (userId != null && userId > 0 && !userId.equals(task.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return task;
    }
}
