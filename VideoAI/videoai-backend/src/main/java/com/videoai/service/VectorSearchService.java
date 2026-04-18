package com.videoai.service;

import com.videoai.model.vo.TranscriptSegmentVO;

import java.util.List;

/**
 * 向量检索服务
 * 支持语义相似度检索和混合检索
 */
public interface VectorSearchService {
    
    /**
     * 索引视频片段(预留向量数据库接口)
     */
    void indexSegments(Long taskId, List<TranscriptSegmentVO> segments);
    
    /**
     * 纯语义检索(基于余弦相似度)
     */
    List<TranscriptSegmentVO> semanticSearch(Long taskId, String query, int limit);
    
    /**
     * 混合检索(关键词 + 语义相似度)
     */
    List<TranscriptSegmentVO> hybridSearch(Long taskId, String query, int limit);
    
    /**
     * 计算两段文本的语义相似度
     */
    double calculateSimilarity(String text1, String text2);
}
