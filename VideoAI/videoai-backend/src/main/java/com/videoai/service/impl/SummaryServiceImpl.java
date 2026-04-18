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
            usageService.recordUsage(task.getUserId(), task.getId(), AiBizTypeEnum.SUMMARY, "summary-" + task.getFileId(),
                    result.getModelName(), 0, 0, 5L, true, true, null);
            return result;
        }
        
        SummaryResult result;
        if (!properties.getAi().isMock() && langChainAgentRunner != null) {
            // 使用 SummaryAgent 智能体生成结构化摘要
            SummaryVO summaryVO = langChainAgentRunner.generateSummary(transcript.getTranscriptText());
            result = convertToSummaryResult(summaryVO, task);
        } else {
            // 降级到传统方式
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
        usageService.recordUsage(task.getUserId(), task.getId(), AiBizTypeEnum.SUMMARY, "summary-" + task.getFileId(),
                result.getModelName(), result.getPromptTokens(), result.getCompletionTokens(),
                result.getDurationMs(), false, true, null);
        return result;
    }
    
    private SummaryResult convertToSummaryResult(SummaryVO summaryVO, VideoTask task) {
        SummaryResult result = new SummaryResult();
        result.setTitle(summaryVO.getTitle());
        result.setSummary(summaryVO.getSummary());
        result.setOutline(summaryVO.getOutline());
        result.setKeywords(summaryVO.getKeywords());
        result.setHighlights(summaryVO.getHighlights());
        result.setQaSuggestions(summaryVO.getQaSuggestions());
        result.setModelName(properties.getAi().getModel());
        // 估算 Token 用量
        int inputTokens = task.getTranscriptText() != null ? task.getTranscriptText().length() / 4 : 0;
        int outputTokens = summaryVO.getSummary() != null ? summaryVO.getSummary().length() / 4 : 0;
        result.setPromptTokens(inputTokens);
        result.setCompletionTokens(outputTokens);
        result.setTotalTokens(inputTokens + outputTokens);
        result.setDurationMs(3000L); // 模拟耗时
        return result;
    }
}
