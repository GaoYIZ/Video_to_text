package com.videoai.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.videoai.mapper.VideoTranscriptSegmentMapper;
import com.videoai.model.entity.VideoTranscriptSegment;
import com.videoai.model.vo.TranscriptSegmentVO;
import com.videoai.service.VectorSearchService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 向量检索服务实现
 * 当前基于 TF-IDF + 余弦相似度实现简化版语义检索
 * 生产环境可接入 Milvus/Chroma/Pinecone 等专业向量数据库
 */
@Service
public class VectorSearchServiceImpl implements VectorSearchService {

    private final VideoTranscriptSegmentMapper segmentMapper;

    public VectorSearchServiceImpl(VideoTranscriptSegmentMapper segmentMapper) {
        this.segmentMapper = segmentMapper;
    }

    @Override
    public void indexSegments(Long taskId, List<TranscriptSegmentVO> segments) {
        // 简化版:直接使用 MySQL 存储
        // TODO: 生产环境应接入向量数据库,存储文本向量嵌入
        for (TranscriptSegmentVO segment : segments) {
            VideoTranscriptSegment entity = new VideoTranscriptSegment();
            entity.setTaskId(taskId);
            entity.setSegmentIndex(segment.getSegmentIndex());
            entity.setStartTimeMs(segment.getStartTimeMs());
            entity.setEndTimeMs(segment.getEndTimeMs());
            entity.setContent(segment.getContent());
            entity.setKeywords(segment.getKeywords());
            segmentMapper.insert(entity);
        }
    }

    @Override
    public List<TranscriptSegmentVO> semanticSearch(Long taskId, String query, int limit) {
        List<VideoTranscriptSegment> allSegments = segmentMapper.selectList(
                Wrappers.<VideoTranscriptSegment>lambdaQuery()
                        .eq(VideoTranscriptSegment::getTaskId, taskId)
        );

        // 计算每个片段与查询的语义相似度
        List<ScoredSegment> scored = allSegments.stream()
                .map(seg -> new ScoredSegment(toVO(seg), calculateSemanticScore(seg, query)))
                .filter(s -> s.score > 0.1) // 过滤低相关度结果
                .sorted(Comparator.comparingDouble((ScoredSegment s) -> -s.score))
                .limit(limit)
                .collect(Collectors.toList());

        return scored.stream().map(s -> s.segment).collect(Collectors.toList());
    }

    @Override
    public List<TranscriptSegmentVO> hybridSearch(Long taskId, String query, int limit) {
        // 分别获取关键词检索和语义检索结果
        List<TranscriptSegmentVO> keywordResults = keywordSearch(taskId, query, limit * 2);
        List<TranscriptSegmentVO> semanticResults = semanticSearch(taskId, query, limit * 2);

        // 使用 Reciprocal Rank Fusion (RRF) 融合两种检索结果
        Map<Long, Double> combinedScores = new HashMap<>();
        
        // 关键词检索得分
        for (int i = 0; i < keywordResults.size(); i++) {
            Long id = keywordResults.get(i).getId();
            double score = 1.0 / (i + 60); // RRF 公式
            combinedScores.merge(id, score, Double::sum);
        }

        // 语义检索得分(权重更高)
        for (int i = 0; i < semanticResults.size(); i++) {
            Long id = semanticResults.get(i).getId();
            double score = 1.5 / (i + 60); // 语义检索权重 1.5x
            combinedScores.merge(id, score, Double::sum);
        }

        // 按综合得分排序,返回 Top-K
        Set<Long> topIds = combinedScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        return keywordResults.stream()
                .filter(seg -> topIds.contains(seg.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public double calculateSimilarity(String text1, String text2) {
        return calculateCosineSimilarity(extractTerms(text1), extractTerms(text2));
    }

    /**
     * 关键词检索(传统 BM25 简化版)
     */
    private List<TranscriptSegmentVO> keywordSearch(Long taskId, String query, int limit) {
        List<String> terms = tokenize(query);
        return segmentMapper.selectList(Wrappers.<VideoTranscriptSegment>lambdaQuery()
                        .eq(VideoTranscriptSegment::getTaskId, taskId))
                .stream()
                .sorted(Comparator.comparingInt(seg -> -calculateKeywordScore(seg, terms)))
                .limit(limit)
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 计算语义相似度得分(TF-IDF 简化版)
     */
    private double calculateSemanticScore(VideoTranscriptSegment segment, String query) {
        List<String> queryTerms = extractTerms(query);
        List<String> segmentTerms = extractTerms(segment.getContent());
        return calculateCosineSimilarity(queryTerms, segmentTerms);
    }

    /**
     * 计算关键词匹配得分
     */
    private int calculateKeywordScore(VideoTranscriptSegment segment, List<String> terms) {
        String content = segment.getContent().toLowerCase(Locale.ROOT);
        int score = 0;
        for (String term : terms) {
            if (content.contains(term)) {
                score += 10;
            }
        }
        return score;
    }

    /**
     * 提取文本词条(分词简化版)
     */
    private List<String> extractTerms(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        String lower = text.toLowerCase(Locale.ROOT);
        return Arrays.asList(lower.split("[\\s,，。？?！!、]+"));
    }

    /**
     * 分词
     */
    private List<String> tokenize(String query) {
        return extractTerms(query);
    }

    /**
     * 计算余弦相似度
     */
    private double calculateCosineSimilarity(List<String> terms1, List<String> terms2) {
        if (terms1.isEmpty() || terms2.isEmpty()) {
            return 0.0;
        }

        // 构建词频向量
        Map<String, Integer> freq1 = buildTermFrequency(terms1);
        Map<String, Integer> freq2 = buildTermFrequency(terms2);

        // 计算所有唯一词条
        Set<String> allTerms = new HashSet<>();
        allTerms.addAll(freq1.keySet());
        allTerms.addAll(freq2.keySet());

        // 计算点积和范数
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String term : allTerms) {
            int f1 = freq1.getOrDefault(term, 0);
            int f2 = freq2.getOrDefault(term, 0);
            dotProduct += f1 * f2;
            norm1 += f1 * f1;
            norm2 += f2 * f2;
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 构建词频映射
     */
    private Map<String, Integer> buildTermFrequency(List<String> terms) {
        Map<String, Integer> freq = new HashMap<>();
        for (String term : terms) {
            freq.merge(term, 1, Integer::sum);
        }
        return freq;
    }

    /**
     * 转换为 VO
     */
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

    /**
     * 带得分的片段
     */
    private static class ScoredSegment {
        TranscriptSegmentVO segment;
        double score;

        ScoredSegment(TranscriptSegmentVO segment, double score) {
            this.segment = segment;
            this.score = score;
        }
    }
}
