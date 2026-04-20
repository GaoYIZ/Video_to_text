package com.videoai.service.impl;

import com.videoai.model.dto.chat.ChatAskRequest;
import com.videoai.model.dto.task.CreateVideoTaskRequest;
import com.videoai.model.dto.task.VideoTaskPageRequest;
import com.videoai.model.dto.workbench.WorkbenchAiActionRequest;
import com.videoai.model.dto.workbench.WorkbenchFileConvertRequest;
import com.videoai.model.dto.workbench.WorkbenchLinkConvertRequest;
import com.videoai.model.vo.ChatAnswerVO;
import com.videoai.model.vo.MonitorEventVO;
import com.videoai.model.vo.PageVO;
import com.videoai.model.vo.SummaryVO;
import com.videoai.model.vo.TaskEventVO;
import com.videoai.model.vo.TaskStatusVO;
import com.videoai.model.vo.UsageOverviewVO;
import com.videoai.model.vo.UsageStatsVO;
import com.videoai.model.vo.UserQuotaVO;
import com.videoai.model.vo.VideoTaskVO;
import com.videoai.model.vo.WorkbenchAiActionVO;
import com.videoai.model.vo.WorkbenchConvertVO;
import com.videoai.model.vo.WorkbenchMonitorVO;
import com.videoai.model.vo.WorkbenchOverviewVO;
import com.videoai.service.ChatService;
import com.videoai.service.UsageService;
import com.videoai.service.VideoAgentService;
import com.videoai.service.VideoResultService;
import com.videoai.service.VideoTaskService;
import com.videoai.service.WorkbenchService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class WorkbenchServiceImpl implements WorkbenchService {

    private final VideoTaskService videoTaskService;
    private final VideoResultService videoResultService;
    private final ChatService chatService;
    private final VideoAgentService videoAgentService;
    private final UsageService usageService;

    public WorkbenchServiceImpl(VideoTaskService videoTaskService,
                                VideoResultService videoResultService,
                                ChatService chatService,
                                VideoAgentService videoAgentService,
                                UsageService usageService) {
        this.videoTaskService = videoTaskService;
        this.videoResultService = videoResultService;
        this.chatService = chatService;
        this.videoAgentService = videoAgentService;
        this.usageService = usageService;
    }

    @Override
    public WorkbenchOverviewVO overview(Long userId) {
        UsageOverviewVO usageOverview = usageService.overview(userId);
        UserQuotaVO quota = usageService.getUserQuota(userId);
        List<VideoTaskVO> recentTasks = recentTasks(userId, 6);
        return WorkbenchOverviewVO.builder()
                .usageOverview(usageOverview)
                .quota(quota)
                .processingTasks((int) recentTasks.stream()
                        .filter(task -> !"SUCCESS".equals(task.getStatusCode()) && !"FAILED".equals(task.getStatusCode()))
                        .count())
                .completedTasks((int) recentTasks.stream().filter(task -> "SUCCESS".equals(task.getStatusCode())).count())
                .failedTasks((int) recentTasks.stream().filter(task -> "FAILED".equals(task.getStatusCode())).count())
                .recentTasks(recentTasks)
                .build();
    }

    @Override
    public WorkbenchMonitorVO monitor(Long userId) {
        List<VideoTaskVO> recentTasks = recentTasks(userId, 10);
        List<VideoTaskVO> sampleTasks = recentTasks(userId, 20);
        Map<String, Integer> distribution = new LinkedHashMap<>();
        for (VideoTaskVO task : sampleTasks) {
            distribution.merge(task.getStatusCode(), 1, Integer::sum);
        }
        List<MonitorEventVO> recentEvents = sampleTasks.stream()
                .limit(6)
                .flatMap(task -> videoTaskService.events(userId, task.getId()).stream()
                        .map(event -> toMonitorEvent(task, event)))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(MonitorEventVO::getCreateTime,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(20)
                .toList();
        return WorkbenchMonitorVO.builder()
                .totalTasks(sampleTasks.size())
                .processingTasks((int) sampleTasks.stream()
                        .filter(task -> !"SUCCESS".equals(task.getStatusCode()) && !"FAILED".equals(task.getStatusCode()))
                        .count())
                .successTasks((int) sampleTasks.stream().filter(task -> "SUCCESS".equals(task.getStatusCode())).count())
                .failedTasks((int) sampleTasks.stream().filter(task -> "FAILED".equals(task.getStatusCode())).count())
                .statusDistribution(distribution)
                .quota(usageService.getUserQuota(userId))
                .dailyStats(usageService.getDailyUsage(userId))
                .monthlyStats(usageService.getMonthlyUsage(userId))
                .recentTasks(recentTasks)
                .recentEvents(recentEvents)
                .build();
    }

    @Override
    public WorkbenchConvertVO convertFile(Long userId, WorkbenchFileConvertRequest request) {
        CreateVideoTaskRequest createRequest = new CreateVideoTaskRequest();
        createRequest.setUploadId(request.getUploadId());
        createRequest.setSessionId(request.getSessionId());
        VideoTaskVO task = videoTaskService.createTask(userId, createRequest);
        return WorkbenchConvertVO.builder()
                .accepted(true)
                .message("解析任务已创建，视频将进入转写、摘要和问答准备流程。")
                .task(task)
                .build();
    }

    @Override
    public WorkbenchConvertVO convertLink(Long userId, WorkbenchLinkConvertRequest request) {
        return WorkbenchConvertVO.builder()
                .accepted(false)
                .message("本地演示环境暂不支持链接直转，请先上传文件再创建解析任务。")
                .build();
    }

    @Override
    public WorkbenchAiActionVO aiAction(Long userId, WorkbenchAiActionRequest request) {
        String action = request.getAction().trim().toLowerCase(Locale.ROOT);
        return switch (action) {
            case "summary" -> buildSummaryAction(userId, request);
            case "agent", "status" -> buildAgentAction(userId, request);
            case "qa", "rag" -> buildQaAction(userId, request);
            default -> buildSummaryAction(userId, request);
        };
    }

    private WorkbenchAiActionVO buildSummaryAction(Long userId, WorkbenchAiActionRequest request) {
        SummaryVO summary = videoResultService.getSummary(userId, request.getTaskId());
        return WorkbenchAiActionVO.builder()
                .action("summary")
                .taskId(request.getTaskId())
                .summary(summary.getSummary())
                .answer(summary.getSummary())
                .highlights(summary.getHighlights())
                .build();
    }

    private WorkbenchAiActionVO buildQaAction(Long userId, WorkbenchAiActionRequest request) {
        ChatAskRequest chatRequest = new ChatAskRequest();
        chatRequest.setTaskId(request.getTaskId());
        chatRequest.setQuestion(request.getQuestion());
        chatRequest.setSessionId(request.getSessionId());
        ChatAnswerVO answer = chatService.ask(userId, chatRequest);
        return WorkbenchAiActionVO.builder()
                .action("qa")
                .taskId(request.getTaskId())
                .sessionId(answer.getSessionId())
                .answer(answer.getAnswer())
                .citedSegments(answer.getCitedSegments())
                .build();
    }

    private WorkbenchAiActionVO buildAgentAction(Long userId, WorkbenchAiActionRequest request) {
        String question = request.getQuestion();
        if (question == null || question.isBlank()) {
            TaskStatusVO status = videoTaskService.status(userId, request.getTaskId());
            question = "请解释这个任务目前处理到哪一步，以及是否存在失败风险。";
            if ("FAILED".equals(status.getStatusCode()) && status.getFailReason() != null) {
                question = "请解释这个任务为什么失败，并给出恢复建议。";
            }
        }
        ChatAskRequest agentRequest = new ChatAskRequest();
        agentRequest.setTaskId(request.getTaskId());
        agentRequest.setQuestion(question);
        agentRequest.setSessionId(request.getSessionId());
        ChatAnswerVO answer = videoAgentService.ask(userId, agentRequest);
        return WorkbenchAiActionVO.builder()
                .action("agent")
                .taskId(request.getTaskId())
                .sessionId(answer.getSessionId())
                .answer(answer.getAnswer())
                .citedSegments(answer.getCitedSegments())
                .build();
    }

    private List<VideoTaskVO> recentTasks(Long userId, long pageSize) {
        VideoTaskPageRequest request = new VideoTaskPageRequest();
        request.setCurrent(1L);
        request.setPageSize(pageSize);
        PageVO<VideoTaskVO> page = videoTaskService.page(userId, request);
        return page.getRecords();
    }

    private MonitorEventVO toMonitorEvent(VideoTaskVO task, TaskEventVO event) {
        if (event == null) {
            return null;
        }
        return MonitorEventVO.builder()
                .taskId(task.getId())
                .taskNo(task.getTaskNo())
                .videoTitle(task.getVideoTitle())
                .step(event.getStep())
                .eventType(event.getEventType())
                .success(event.getSuccess())
                .detail(event.getDetail())
                .createTime(event.getCreateTime())
                .build();
    }
}
