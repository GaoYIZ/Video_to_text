package com.videoai.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.videoai.mapper.VideoTranscriptSegmentMapper;
import com.videoai.model.entity.VideoTranscriptSegment;
import com.videoai.model.vo.TranscriptSegmentVO;
import com.videoai.service.RagService;
import com.videoai.service.VectorSearchService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * RAG 检索服务实现
 * 支持关键词检索、语义检索和混合检索
 */
@Service
public class RagServiceImpl implements RagService {

    private final VideoTranscriptSegmentMapper segmentMapper;
    private final VectorSearchService vectorSearchService;

    public RagServiceImpl(VideoTranscriptSegmentMapper segmentMapper, 
                         VectorSearchService vectorSearchService) {
        this.segmentMapper = segmentMapper;
        this.vectorSearchService = vectorSearchService;
    }

    @Override
    public void indexSegments(Long taskId, List<VideoTranscriptSegment> segments) {
        for (VideoTranscriptSegment segment : segments) {
            segmentMapper.insert(segment);
        }
    }

    @Override
    public List<TranscriptSegmentVO> retrieve(Long taskId, String query, int limit) {
        // 默认使用混合检索,兼顾准确性和召回率
        return hybridRetrieve(taskId, query, limit);
    }

    @Override
    public List<TranscriptSegmentVO> locateByKeyword(Long taskId, String keyword, int limit) {
        String lower = keyword.toLowerCase(Locale.ROOT);
        return segmentMapper.selectList(Wrappers.<VideoTranscriptSegment>lambdaQuery()
                        .eq(VideoTranscriptSegment::getTaskId, taskId))
                .stream()
                .filter(segment -> segment.getContent() != null && 
                        segment.getContent().toLowerCase(Locale.ROOT).contains(lower))
                .limit(limit)
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TranscriptSegmentVO> semanticRetrieve(Long taskId, String query, int limit) {
        return vectorSearchService.semanticSearch(taskId, query, limit);
    }

    @Override
    public List<TranscriptSegmentVO> hybridRetrieve(Long taskId, String query, int limit) {
        return vectorSearchService.hybridSearch(taskId, query, limit);
    }

    private TranscriptSegmentVO toVO(VideoTranscriptSegment segment) {
        return TranscriptSegmentVO.builder()
                .id(segment.getId())
                .segmentIndex(segment.getSegmentIndex())
                .startTimeMs(segment.getStartTimeMs())
                .endTimeMs(segment.getEndTimeMs())
                .content(segment.getContent())
                .keywords(segment.getKeywords())
                .build();
    }
}
