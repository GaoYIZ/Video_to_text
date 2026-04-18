package com.videoai.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.videoai.mapper.VideoTranscriptSegmentMapper;
import com.videoai.model.entity.VideoTranscriptSegment;
import com.videoai.model.vo.TranscriptSegmentVO;
import com.videoai.service.VectorSearchService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class VectorSearchServiceImpl implements VectorSearchService {

    private static final double SEMANTIC_THRESHOLD = 0.1D;
    private static final int RRF_K = 60;
    private static final double SEMANTIC_WEIGHT = 1.5D;

    private final VideoTranscriptSegmentMapper segmentMapper;

    public VectorSearchServiceImpl(VideoTranscriptSegmentMapper segmentMapper) {
        this.segmentMapper = segmentMapper;
    }

    @Override
    public void indexSegments(Long taskId, List<TranscriptSegmentVO> segments) {
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
                        .eq(VideoTranscriptSegment::getTaskId, taskId));
        return allSegments.stream()
                .map(segment -> new ScoredSegment(toVO(segment), calculateSemanticScore(segment.getContent(), query)))
                .filter(segment -> segment.score >= SEMANTIC_THRESHOLD)
                .sorted(Comparator.comparingDouble((ScoredSegment segment) -> segment.score).reversed())
                .limit(limit)
                .map(ScoredSegment::segment)
                .toList();
    }

    @Override
    public List<TranscriptSegmentVO> hybridSearch(Long taskId, String query, int limit) {
        List<TranscriptSegmentVO> keywordResults = keywordSearch(taskId, query, Math.max(limit * 2, limit));
        List<TranscriptSegmentVO> semanticResults = semanticSearch(taskId, query, Math.max(limit * 2, limit));

        Map<Long, TranscriptSegmentVO> candidates = new LinkedHashMap<>();
        keywordResults.forEach(segment -> candidates.put(segment.getId(), segment));
        semanticResults.forEach(segment -> candidates.put(segment.getId(), segment));

        Map<Long, Double> scores = new HashMap<>();
        for (int i = 0; i < keywordResults.size(); i++) {
            scores.merge(keywordResults.get(i).getId(), reciprocalRankScore(i), Double::sum);
        }
        for (int i = 0; i < semanticResults.size(); i++) {
            scores.merge(semanticResults.get(i).getId(), reciprocalRankScore(i) * SEMANTIC_WEIGHT, Double::sum);
        }

        return candidates.values().stream()
                .sorted(Comparator.comparingDouble((TranscriptSegmentVO segment) -> scores.getOrDefault(segment.getId(), 0D)).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    public double calculateSimilarity(String text1, String text2) {
        return calculateCosineSimilarity(extractTerms(text1), extractTerms(text2));
    }

    private List<TranscriptSegmentVO> keywordSearch(Long taskId, String query, int limit) {
        List<String> terms = tokenize(query);
        return segmentMapper.selectList(Wrappers.<VideoTranscriptSegment>lambdaQuery()
                        .eq(VideoTranscriptSegment::getTaskId, taskId))
                .stream()
                .map(segment -> new ScoredSegment(toVO(segment), keywordScore(segment.getContent(), terms)))
                .filter(segment -> segment.score > 0)
                .sorted(Comparator.comparingDouble((ScoredSegment segment) -> segment.score).reversed())
                .limit(limit)
                .map(ScoredSegment::segment)
                .toList();
    }

    private double keywordScore(String content, List<String> terms) {
        String lower = content == null ? "" : content.toLowerCase(Locale.ROOT);
        double score = 0D;
        for (String term : terms) {
            if (!term.isBlank() && lower.contains(term)) {
                score += 10D;
            }
        }
        return score;
    }

    private double calculateSemanticScore(String content, String query) {
        return calculateCosineSimilarity(extractTerms(content), extractTerms(query));
    }

    private List<String> extractTerms(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(text.toLowerCase(Locale.ROOT)
                        .split("[\\s\\p{Punct}，。！？；：、“”‘’（）【】《》]+"))
                .filter(term -> !term.isBlank())
                .toList();
    }

    private List<String> tokenize(String query) {
        return new ArrayList<>(extractTerms(query));
    }

    private double calculateCosineSimilarity(List<String> terms1, List<String> terms2) {
        if (terms1.isEmpty() || terms2.isEmpty()) {
            return 0D;
        }

        Map<String, Integer> freq1 = buildTermFrequency(terms1);
        Map<String, Integer> freq2 = buildTermFrequency(terms2);
        Set<String> allTerms = new HashSet<>();
        allTerms.addAll(freq1.keySet());
        allTerms.addAll(freq2.keySet());

        double dotProduct = 0D;
        double norm1 = 0D;
        double norm2 = 0D;
        for (String term : allTerms) {
            int value1 = freq1.getOrDefault(term, 0);
            int value2 = freq2.getOrDefault(term, 0);
            dotProduct += (double) value1 * value2;
            norm1 += (double) value1 * value1;
            norm2 += (double) value2 * value2;
        }
        if (norm1 == 0D || norm2 == 0D) {
            return 0D;
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private Map<String, Integer> buildTermFrequency(List<String> terms) {
        Map<String, Integer> frequencies = new HashMap<>();
        for (String term : terms) {
            frequencies.merge(term, 1, Integer::sum);
        }
        return frequencies;
    }

    private double reciprocalRankScore(int rank) {
        return 1D / (rank + RRF_K);
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

    private record ScoredSegment(TranscriptSegmentVO segment, double score) {
    }
}
