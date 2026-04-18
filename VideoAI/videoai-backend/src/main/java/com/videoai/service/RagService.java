package com.videoai.service;

import com.videoai.model.entity.VideoTranscriptSegment;
import com.videoai.model.vo.TranscriptSegmentVO;

import java.util.List;

/**
 * RAG 检索服务
 * 支持关键词检索、语义检索和混合检索
 */
public interface RagService {

    /**
     * 索引视频片段
     */
    void indexSegments(Long taskId, List<VideoTranscriptSegment> segments);

    /**
     * 检索相关片段(默认使用混合检索)
     */
    List<TranscriptSegmentVO> retrieve(Long taskId, String query, int limit);

    /**
     * 按关键词定位片段
     */
    List<TranscriptSegmentVO> locateByKeyword(Long taskId, String keyword, int limit);

    /**
     * 纯语义检索
     */
    List<TranscriptSegmentVO> semanticRetrieve(Long taskId, String query, int limit);

    /**
     * 混合检索(关键词 + 语义)
     */
    List<TranscriptSegmentVO> hybridRetrieve(Long taskId, String query, int limit);
}
