package com.videoai.ai.client.impl;

import com.videoai.ai.client.DeepSeekClient;
import com.videoai.ai.model.QuestionAnswerResult;
import com.videoai.ai.model.SummaryResult;
import com.videoai.common.util.JsonUtils;
import com.videoai.config.VideoAiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "videoai.ai", name = "mock", havingValue = "false")
public class DeepSeekClientImpl implements DeepSeekClient {

    private final VideoAiProperties properties;
    private final RestClient restClient;

    public DeepSeekClientImpl(VideoAiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getAi().getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAi().getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public SummaryResult summarize(String transcriptText) {
        Instant start = Instant.now();
        String prompt = """
                请基于以下转写内容输出 JSON，字段为：
                title, summary, outline, keywords, highlights, qa_suggestions。
                转写内容：
                %s
                """.formatted(transcriptText);
        Map<String, Object> response = createCompletion(List.of(
                Map.of("role", "system", "content", "你是视频内容总结助手，只返回 JSON。"),
                Map.of("role", "user", "content", prompt)
        ));
        String content = extractContent(response);
        Map<String, Object> json = JsonUtils.toMap(content);
        return SummaryResult.builder()
                .title(String.valueOf(json.getOrDefault("title", "视频总结")))
                .summary(String.valueOf(json.getOrDefault("summary", "")))
                .outline(castList(json.get("outline")))
                .keywords(castList(json.get("keywords")))
                .highlights(castList(json.get("highlights")))
                .qaSuggestions(castList(json.get("qa_suggestions")))
                .modelName(properties.getAi().getModel())
                .promptTokens(readUsage(response, "prompt_tokens"))
                .completionTokens(readUsage(response, "completion_tokens"))
                .totalTokens(readUsage(response, "total_tokens"))
                .durationMs(Duration.between(start, Instant.now()).toMillis())
                .build();
    }

    @Override
    public QuestionAnswerResult answer(String systemPrompt, String userPrompt, List<String> contextSegments) {
        Instant start = Instant.now();
        String content = "相关片段：\n" + String.join("\n", contextSegments) + "\n\n问题：" + userPrompt;
        Map<String, Object> response = createCompletion(List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", content)
        ));
        return QuestionAnswerResult.builder()
                .answer(extractContent(response))
                .modelName(properties.getAi().getModel())
                .promptTokens(readUsage(response, "prompt_tokens"))
                .completionTokens(readUsage(response, "completion_tokens"))
                .totalTokens(readUsage(response, "total_tokens"))
                .durationMs(Duration.between(start, Instant.now()).toMillis())
                .cacheHit(false)
                .build();
    }

    private Map<String, Object> createCompletion(List<Map<String, String>> messages) {
        return restClient.post()
                .uri("/chat/completions")
                .body(Map.of(
                        "model", properties.getAi().getModel(),
                        "messages", messages,
                        "temperature", 0.2,
                        "stream", false
                ))
                .retrieve()
                .body(Map.class);
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return String.valueOf(message.getOrDefault("content", ""));
    }

    @SuppressWarnings("unchecked")
    private Integer readUsage(Map<String, Object> response, String key) {
        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        if (usage == null) {
            return 0;
        }
        Object value = usage.get(key);
        return value == null ? 0 : Integer.parseInt(String.valueOf(value));
    }

    @SuppressWarnings("unchecked")
    private List<String> castList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
