package com.videoai.service.impl;

import com.videoai.agent.LangChainAgentRunner;
import com.videoai.agent.VideoAgentTools;
import com.videoai.common.constant.RedisKeyConstants;
import com.videoai.common.enums.ErrorCode;
import com.videoai.common.exception.BusinessException;
import com.videoai.common.util.DigestUtils;
import com.videoai.common.util.JsonUtils;
import com.videoai.config.VideoAiProperties;
import com.videoai.mapper.ChatMessageMapper;
import com.videoai.mapper.VideoTaskMapper;
import com.videoai.model.dto.chat.ChatAskRequest;
import com.videoai.model.entity.ChatMessage;
import com.videoai.model.entity.VideoTask;
import com.videoai.model.enums.AiBizTypeEnum;
import com.videoai.model.enums.ChatRoleEnum;
import com.videoai.model.vo.ChatAnswerVO;
import com.videoai.model.vo.TaskEventVO;
import com.videoai.model.vo.TaskStatusVO;
import com.videoai.model.vo.TranscriptSegmentVO;
import com.videoai.service.RagService;
import com.videoai.service.RateLimitService;
import com.videoai.service.UsageService;
import com.videoai.service.VideoAgentService;
import com.videoai.service.VideoResultService;
import com.videoai.service.VideoTaskService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class VideoAgentServiceImpl implements VideoAgentService {

    private final VideoTaskMapper videoTaskMapper;
    private final VideoTaskService videoTaskService;
    private final VideoResultService videoResultService;
    private final RagService ragService;
    private final VideoAgentTools videoAgentTools;
    private final LangChainAgentRunner langChainAgentRunner;
    private final VideoAiProperties properties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatMessageMapper chatMessageMapper;
    private final UsageService usageService;
    private final RateLimitService rateLimitService;

    public VideoAgentServiceImpl(VideoTaskMapper videoTaskMapper, VideoTaskService videoTaskService,
                                 VideoResultService videoResultService, RagService ragService,
                                 VideoAgentTools videoAgentTools, ObjectProvider<LangChainAgentRunner> runnerProvider,
                                 VideoAiProperties properties, RedisTemplate<String, Object> redisTemplate,
                                 ChatMessageMapper chatMessageMapper, UsageService usageService,
                                 RateLimitService rateLimitService) {
        this.videoTaskMapper = videoTaskMapper;
        this.videoTaskService = videoTaskService;
        this.videoResultService = videoResultService;
        this.ragService = ragService;
        this.videoAgentTools = videoAgentTools;
        this.langChainAgentRunner = runnerProvider.getIfAvailable();
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        this.chatMessageMapper = chatMessageMapper;
        this.usageService = usageService;
        this.rateLimitService = rateLimitService;
    }

    @Override
    public ChatAnswerVO ask(Long userId, ChatAskRequest request) {
        VideoTask task = checkTask(userId, request.getTaskId());
        rateLimitService.acquire(userId, 1, 20, 60);
        usageService.checkQuota(userId);
        String sessionId = request.getSessionId() == null || request.getSessionId().isBlank()
                ? "agent-" + task.getId()
                : request.getSessionId();
        String questionHash = DigestUtils.md5("agent:" + request.getQuestion().trim().toLowerCase());
        String cacheKey = RedisKeyConstants.AI_QA_CACHE.formatted(task.getId(), questionHash);
        Object cache = redisTemplate.opsForValue().get(cacheKey);
        if (cache instanceof String json && !json.isBlank()) {
            usageService.recordUsage(userId, task.getId(), AiBizTypeEnum.AGENT_QA, questionHash,
                    "agent-cache", 0, 0, 5L, true, true, null);
            return JsonUtils.fromJson(json, ChatAnswerVO.class);
        }

        String answer;
        List<TranscriptSegmentVO> citedSegments = ragService.retrieve(task.getId(), request.getQuestion(), 5);
        if (!properties.getAi().isMock() && langChainAgentRunner != null) {
            answer = langChainAgentRunner.runVideoQa(sessionId, task.getId(), request.getQuestion());
        } else {
            answer = buildMockAgentAnswer(task.getId(), request.getQuestion(), citedSegments);
        }
        ChatAnswerVO response = ChatAnswerVO.builder()
                .sessionId(sessionId)
                .answer(answer)
                .citedSegments(citedSegments)
                .fromCache(false)
                .build();
        redisTemplate.opsForValue().set(cacheKey, JsonUtils.toJson(response), Duration.ofHours(6));
        saveAgentConversation(userId, task.getId(), sessionId, request.getQuestion(), answer, citedSegments);
        usageService.recordUsage(userId, task.getId(), AiBizTypeEnum.AGENT_QA, questionHash,
                properties.getAi().isMock() ? "mock-agent" : properties.getAi().getModel(),
                160, 120, 300L, false, true, null);
        return response;
    }

    @Override
    public Map<String, Object> debugTools(Long userId, Long taskId, String keyword) {
        checkTask(userId, taskId);
        return videoAgentTools.debug(taskId, keyword);
    }

    private String buildMockAgentAnswer(Long taskId, String question, List<TranscriptSegmentVO> citedSegments) {
        String lower = question.toLowerCase();
        if (lower.contains("状态") || lower.contains("失败") || lower.contains("进度")) {
            TaskStatusVO status = videoTaskService.status(null, taskId);
            List<TaskEventVO> events = videoTaskService.events(null, taskId);
            String latestEvent = events.isEmpty() ? "暂无事件" : events.get(events.size() - 1).getDetail();
            return "任务当前状态为 " + status.getStatusCode() + "，当前步骤是 " + status.getCurrentStep()
                    + "，进度约 " + status.getProgressPercent() + "%。最近一次事件：" + latestEvent;
        }
        String summaryText = videoResultService.getSummary(null, taskId) == null
                ? "暂无摘要"
                : videoResultService.getSummary(null, taskId).getSummary();
        String segmentText = citedSegments.isEmpty() ? "暂无命中片段" : citedSegments.get(0).getContent();
        return """
                Video Analysis Agent 已综合摘要、转写片段与任务上下文完成回答。
                摘要结论：%s
                重点片段：%s
                针对你的问题“%s”，可以确认这段视频核心关注上传、异步处理、Agent 工具调用与 RAG 检索增强。
                """.formatted(summaryText, segmentText, question);
    }

    private void saveAgentConversation(Long userId, Long taskId, String sessionId, String question,
                                       String answer, List<TranscriptSegmentVO> citedSegments) {
        String key = RedisKeyConstants.CHAT_MEMORY.formatted(userId, taskId, sessionId);
        redisTemplate.opsForList().rightPush(key, "USER: " + question);
        redisTemplate.opsForList().rightPush(key, "AGENT: " + answer);
        redisTemplate.expire(key, Duration.ofDays(7));

        ChatMessage userMessage = new ChatMessage();
        userMessage.setTaskId(taskId);
        userMessage.setUserId(userId);
        userMessage.setSessionId(sessionId);
        userMessage.setRole(ChatRoleEnum.USER.name());
        userMessage.setMessageType("AGENT_QA");
        userMessage.setContent(question);
        chatMessageMapper.insert(userMessage);

        ChatMessage agentMessage = new ChatMessage();
        agentMessage.setTaskId(taskId);
        agentMessage.setUserId(userId);
        agentMessage.setSessionId(sessionId);
        agentMessage.setRole(ChatRoleEnum.ASSISTANT.name());
        agentMessage.setMessageType("AGENT_QA");
        agentMessage.setContent(answer);
        agentMessage.setCitedSegmentsJson(JsonUtils.toJson(citedSegments));
        agentMessage.setModelName(properties.getAi().isMock() ? "mock-agent" : properties.getAi().getModel());
        agentMessage.setTotalTokens(280);
        chatMessageMapper.insert(agentMessage);
    }

    private VideoTask checkTask(Long userId, Long taskId) {
        VideoTask task = videoTaskMapper.selectById(taskId);
        if (task == null || !userId.equals(task.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }
        return task;
    }
}
