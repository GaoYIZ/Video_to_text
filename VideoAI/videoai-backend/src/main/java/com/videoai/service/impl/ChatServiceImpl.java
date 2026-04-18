package com.videoai.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.videoai.ai.client.DeepSeekClient;
import com.videoai.ai.model.QuestionAnswerResult;
import com.videoai.common.constant.RedisKeyConstants;
import com.videoai.common.enums.ErrorCode;
import com.videoai.common.exception.BusinessException;
import com.videoai.common.util.DigestUtils;
import com.videoai.common.util.JsonUtils;
import com.videoai.mapper.ChatMessageMapper;
import com.videoai.mapper.VideoTaskMapper;
import com.videoai.model.dto.chat.ChatAskRequest;
import com.videoai.model.entity.ChatMessage;
import com.videoai.model.entity.VideoTask;
import com.videoai.model.enums.AiBizTypeEnum;
import com.videoai.model.enums.ChatRoleEnum;
import com.videoai.model.vo.ChatAnswerVO;
import com.videoai.model.vo.PageVO;
import com.videoai.model.vo.TranscriptSegmentVO;
import com.videoai.service.ChatService;
import com.videoai.service.RagService;
import com.videoai.service.RateLimitService;
import com.videoai.service.UsageService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final VideoTaskMapper videoTaskMapper;
    private final RagService ragService;
    private final DeepSeekClient deepSeekClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatMessageMapper chatMessageMapper;
    private final UsageService usageService;
    private final RateLimitService rateLimitService;

    public ChatServiceImpl(VideoTaskMapper videoTaskMapper, RagService ragService, DeepSeekClient deepSeekClient,
                           RedisTemplate<String, Object> redisTemplate, ChatMessageMapper chatMessageMapper,
                           UsageService usageService, RateLimitService rateLimitService) {
        this.videoTaskMapper = videoTaskMapper;
        this.ragService = ragService;
        this.deepSeekClient = deepSeekClient;
        this.redisTemplate = redisTemplate;
        this.chatMessageMapper = chatMessageMapper;
        this.usageService = usageService;
        this.rateLimitService = rateLimitService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatAnswerVO ask(Long userId, ChatAskRequest request) {
        VideoTask task = checkTask(userId, request.getTaskId());
        rateLimitService.acquire(userId, 1, 20, 60);
        usageService.checkQuota(userId);
        String sessionId = buildSessionId(request, task);
        String questionHash = DigestUtils.md5(request.getQuestion().trim().toLowerCase());
        String cacheKey = RedisKeyConstants.AI_QA_CACHE.formatted(task.getId(), questionHash);
        Object cache = redisTemplate.opsForValue().get(cacheKey);
        if (cache instanceof String json && !json.isBlank()) {
            usageService.recordUsage(userId, task.getId(), AiBizTypeEnum.QA, questionHash, "cache", 0, 0, 5L, true, true, null);
            return JsonUtils.fromJson(json, ChatAnswerVO.class);
        }

        List<TranscriptSegmentVO> segments = ragService.retrieve(task.getId(), request.getQuestion(), 5);
        List<String> history = readMemory(userId, task.getId(), sessionId);
        String userPrompt = """
                历史上下文：
                %s

                当前问题：
                %s
                """.formatted(String.join("\n", history), request.getQuestion());
        QuestionAnswerResult answerResult = deepSeekClient.answer(
                "你是 VideoAI 的视频问答助手，请结合检索片段回答，无法确认时请明确说明。",
                userPrompt,
                segments.stream().map(TranscriptSegmentVO::getContent).toList()
        );
        ChatAnswerVO response = ChatAnswerVO.builder()
                .sessionId(sessionId)
                .answer(answerResult.getAnswer())
                .citedSegments(segments)
                .fromCache(false)
                .build();
        redisTemplate.opsForValue().set(cacheKey, JsonUtils.toJson(response), Duration.ofHours(6));
        saveConversation(userId, task.getId(), sessionId, request.getQuestion(), response.getAnswer(),
                answerResult.getTotalTokens(), answerResult.getModelName(), segments);
        usageService.recordUsage(userId, task.getId(), AiBizTypeEnum.QA, questionHash, answerResult.getModelName(),
                answerResult.getPromptTokens(), answerResult.getCompletionTokens(), answerResult.getDurationMs(),
                false, true, null);
        return response;
    }

    @Override
    public PageVO<String> history(Long userId, Long taskId, String sessionId) {
        checkTask(userId, taskId);
        List<String> messages = readMemory(userId, taskId, sessionId);
        return PageVO.<String>builder()
                .current(1L)
                .pageSize((long) messages.size())
                .total((long) messages.size())
                .records(messages)
                .build();
    }

    private VideoTask checkTask(Long userId, Long taskId) {
        VideoTask task = videoTaskMapper.selectById(taskId);
        if (task == null || !userId.equals(task.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }
        return task;
    }

    private String buildSessionId(ChatAskRequest request, VideoTask task) {
        return request.getSessionId() == null || request.getSessionId().isBlank()
                ? "session-" + task.getId()
                : request.getSessionId();
    }

    private List<String> readMemory(Long userId, Long taskId, String sessionId) {
        String key = RedisKeyConstants.CHAT_MEMORY.formatted(userId, taskId, sessionId);
        List<Object> values = redisTemplate.opsForList().range(key, 0, -1);
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }
        return values.stream().map(String::valueOf).toList();
    }

    private void saveConversation(Long userId, Long taskId, String sessionId, String question, String answer,
                                  Integer totalTokens, String modelName, List<TranscriptSegmentVO> citedSegments) {
        String key = RedisKeyConstants.CHAT_MEMORY.formatted(userId, taskId, sessionId);
        redisTemplate.opsForList().rightPush(key, "USER: " + question);
        redisTemplate.opsForList().rightPush(key, "ASSISTANT: " + answer);
        redisTemplate.expire(key, Duration.ofDays(7));

        ChatMessage userMessage = new ChatMessage();
        userMessage.setTaskId(taskId);
        userMessage.setUserId(userId);
        userMessage.setSessionId(sessionId);
        userMessage.setRole(ChatRoleEnum.USER.name());
        userMessage.setMessageType("QA");
        userMessage.setContent(question);
        userMessage.setTotalTokens(0);
        chatMessageMapper.insert(userMessage);

        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setTaskId(taskId);
        assistantMessage.setUserId(userId);
        assistantMessage.setSessionId(sessionId);
        assistantMessage.setRole(ChatRoleEnum.ASSISTANT.name());
        assistantMessage.setMessageType("QA");
        assistantMessage.setContent(answer);
        assistantMessage.setCitedSegmentsJson(JsonUtils.toJson(citedSegments));
        assistantMessage.setModelName(modelName);
        assistantMessage.setTotalTokens(totalTokens);
        chatMessageMapper.insert(assistantMessage);
    }
}
