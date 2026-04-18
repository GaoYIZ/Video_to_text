package com.videoai.service.impl;

import com.videoai.agent.LangChainAgentRunner;
import com.videoai.ai.client.DeepSeekClient;
import com.videoai.ai.model.SummaryResult;
import com.videoai.common.constant.RedisKeyConstants;
import com.videoai.common.util.JsonUtils;
import com.videoai.common.util.RetryUtils;
import com.videoai.config.VideoAiProperties;
import com.videoai.mapper.VideoSummaryMapper;
import com.videoai.model.entity.VideoSummary;
import com.videoai.model.entity.VideoTask;
import com.videoai.model.entity.VideoTranscript;
import com.videoai.model.enums.AiBizTypeEnum;
import com.videoai.model.vo.SummaryVO;
import com.videoai.service.SummaryService;
import com.videoai.service.UsageService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class SummaryServiceImpl implements SummaryService {

    private final DeepSeekClient deepSeekClient;
    private final VideoAiProperties properties;
    private final VideoSummaryMapper videoSummaryMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UsageService usageService;
    private final LangChainAgentRunner langChainAgentRunner;

    public SummaryServiceImpl(DeepSeekClient deepSeekClient, VideoAiProperties properties,
                              VideoSummaryMapper videoSummaryMapper, RedisTemplate<String, Object> redisTemplate,
                              UsageService usageService, ObjectProvider<LangChainAgentRunner> runnerProvider) {
        this.deepSeekClient = deepSeekClient;
        this.properties = properties;
        this.videoSummaryMapper = videoSummaryMapper;
        this.redisTemplate = redisTemplate;
        this.usageService = usageService;
        this.langChainAgentRunner = runnerProvider.getIfAvailable();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SummaryResult summarize(VideoTask task, VideoTranscript transcript) {
        String cacheKey = RedisKeyConstants.AI_SUMMARY_CACHE.formatted(task.getFileId());
        Object cache = redisTemplate.opsForValue().get(cacheKey);
        if (cache instanceof String json && !json.isBlank()) {
            SummaryResult result = JsonUtils.fromJson(json, SummaryResult.class);
            usageService.recordUsage(task.getUserId(), task.getId(), AiBizTypeEnum.SUMMARY,
                    "summary-" + task.getFileId(), result.getModelName(), 0, 0, 5L, true, true, null);
            return result;
        }

        SummaryResult result;
        if (!properties.getAi().isMock() && langChainAgentRunner != null) {
            SummaryVO summaryVO = langChainAgentRunner.generateSummary(transcript.getTranscriptText());
            result = convertToSummaryResult(summaryVO, transcript.getTranscriptText());
        } else {
            result = RetryUtils.execute(() -> deepSeekClient.summarize(transcript.getTranscriptText()),
                    properties.getAi().getMaxRetries(), 400L);
        }

        VideoSummary summary = new VideoSummary();
        summary.setTaskId(task.getId());
        summary.setFileId(task.getFileId());
        summary.setTitle(result.getTitle());
        summary.setSummary(result.getSummary());
        summary.setOutlineJson(JsonUtils.toJson(result.getOutline()));
        summary.setKeywordsJson(JsonUtils.toJson(result.getKeywords()));
        summary.setHighlightsJson(JsonUtils.toJson(result.getHighlights()));
        summary.setQaSuggestionsJson(JsonUtils.toJson(result.getQaSuggestions()));
        summary.setModelName(result.getModelName());
        summary.setPromptTokens(result.getPromptTokens());
        summary.setCompletionTokens(result.getCompletionTokens());
        summary.setTotalTokens(result.getTotalTokens());
        summary.setCostTimeMs(result.getDurationMs());
        videoSummaryMapper.insert(summary);

        redisTemplate.opsForValue().set(cacheKey, JsonUtils.toJson(result), Duration.ofHours(12));
        usageService.recordUsage(task.getUserId(), task.getId(), AiBizTypeEnum.SUMMARY,
                "summary-" + task.getFileId(), result.getModelName(), result.getPromptTokens(),
                result.getCompletionTokens(), result.getDurationMs(), false, true, null);
        return result;
    }

    private SummaryResult convertToSummaryResult(SummaryVO summaryVO, String transcriptText) {
        int inputTokens = transcriptText == null ? 0 : Math.max(1, transcriptText.length() / 4);
        int outputTokens = estimateOutputTokens(summaryVO);
        return SummaryResult.builder()
                .title(summaryVO.getTitle())
                .summary(summaryVO.getSummary())
                .outline(summaryVO.getOutline())
                .keywords(summaryVO.getKeywords())
                .highlights(summaryVO.getHighlights())
                .qaSuggestions(summaryVO.getQaSuggestions())
                .modelName(properties.getAi().getModel())
                .promptTokens(inputTokens)
                .completionTokens(outputTokens)
                .totalTokens(inputTokens + outputTokens)
                .durationMs(3000L)
                .build();
    }

    private int estimateOutputTokens(SummaryVO summaryVO) {
        int length = 0;
        length += safeLength(summaryVO.getTitle());
        length += safeLength(summaryVO.getSummary());
        length += summaryVO.getOutline() == null ? 0 : summaryVO.getOutline().stream().mapToInt(this::safeLength).sum();
        length += summaryVO.getKeywords() == null ? 0 : summaryVO.getKeywords().stream().mapToInt(this::safeLength).sum();
        length += summaryVO.getHighlights() == null ? 0 : summaryVO.getHighlights().stream().mapToInt(this::safeLength).sum();
        length += summaryVO.getQaSuggestions() == null ? 0 : summaryVO.getQaSuggestions().stream().mapToInt(this::safeLength).sum();
        return Math.max(1, length / 4);
    }

    private int safeLength(String value) {
        return value == null ? 0 : value.length();
    }
}
