package com.videoai.agent;

import com.videoai.common.util.JsonUtils;
import com.videoai.model.entity.VideoTask;
import com.videoai.model.vo.SummaryVO;
import com.videoai.model.vo.TaskEventVO;
import com.videoai.model.vo.TaskStatusVO;
import com.videoai.model.vo.TranscriptSegmentVO;
import com.videoai.model.vo.TranscriptVO;
import com.videoai.model.vo.VideoTaskDetailVO;
import com.videoai.service.RagService;
import com.videoai.service.VideoResultService;
import com.videoai.service.VideoTaskService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class VideoAgentTools {

    private final VideoTaskService videoTaskService;
    private final VideoResultService videoResultService;
    private final RagService ragService;

    public VideoAgentTools(VideoTaskService videoTaskService, VideoResultService videoResultService, RagService ragService) {
        this.videoTaskService = videoTaskService;
        this.videoResultService = videoResultService;
        this.ragService = ragService;
    }

    @Tool("查询视频基础信息")
    public String getVideoBasicInfo(Long taskId) {
        VideoTaskDetailVO detail = videoTaskService.detail(null, taskId);
        return JsonUtils.toJson(detail);
    }

    @Tool("查询任务状态")
    public String getTaskStatus(Long taskId) {
        TaskStatusVO status = videoTaskService.status(null, taskId);
        return JsonUtils.toJson(status);
    }

    @Tool("查询任务事件日志")
    public String getTaskEvents(Long taskId) {
        List<TaskEventVO> events = videoTaskService.events(null, taskId);
        return JsonUtils.toJson(events);
    }

    @Tool("查询完整转写文本")
    public String getTranscript(Long taskId) {
        TranscriptVO transcript = videoResultService.getTranscript(null, taskId);
        return transcript == null ? "" : transcript.getTranscriptText();
    }

    @Tool("查询 AI 摘要结果")
    public String getSummary(Long taskId) {
        SummaryVO summary = videoResultService.getSummary(null, taskId);
        return JsonUtils.toJson(summary);
    }

    @Tool("检索与问题相关的视频片段")
    public String searchSegments(Long taskId, String question) {
        // 使用混合检索,提升召回准确率
        List<TranscriptSegmentVO> segments = ragService.hybridRetrieve(taskId, question, 5);
        return JsonUtils.toJson(segments);
    }

    @Tool("按关键词定位视频片段")
    public String locateByKeyword(Long taskId, String keyword) {
        List<TranscriptSegmentVO> segments = ragService.locateByKeyword(taskId, keyword, 5);
        return JsonUtils.toJson(segments);
    }

    @Tool("查询任务失败原因和建议")
    public String getTaskFailureReason(Long taskId) {
        VideoTaskDetailVO detail = videoTaskService.detail(null, taskId);
        if (detail == null || !"FAILED".equals(detail.getStatusCode())) {
            return "任务未失败,当前状态: " + (detail == null ? "未知" : detail.getStatusCode());
        }
        List<TaskEventVO> events = videoTaskService.events(null, taskId);
        TaskEventVO lastError = events.stream()
                .filter(e -> "ERROR".equals(e.getEventType()))
                .reduce((first, second) -> second)
                .orElse(null);
        if (lastError == null) {
            return "任务失败但未记录具体错误信息";
        }
        return String.format(
                "失败步骤: %s\n错误类型: %s\n错误详情: %s\n发生时间: %s\n建议: %s",
                lastError.getCurrentStep(),
                lastError.getEventType(),
                lastError.getDetail(),
                lastError.getCreateTime(),
                generateSuggestion(lastError)
        );
    }

    @Tool("获取任务处理耗时统计")
    public String getTaskDurationStats(Long taskId) {
        List<TaskEventVO> events = videoTaskService.events(null, taskId);
        if (events.isEmpty()) {
            return "暂无事件记录";
        }
        
        Map<String, Long> stepDurations = new HashMap<>();
        for (int i = 1; i < events.size(); i++) {
            TaskEventVO prev = events.get(i - 1);
            TaskEventVO curr = events.get(i);
            long duration = Duration.between(prev.getCreateTime(), curr.getCreateTime()).toSeconds();
            stepDurations.put(prev.getCurrentStep() + "->" + curr.getCurrentStep(), duration);
        }
        
        long totalDuration = Duration.between(
                events.get(0).getCreateTime(),
                events.get(events.size() - 1).getCreateTime()
        ).toSeconds();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSeconds", totalDuration);
        stats.put("stepDurations", stepDurations);
        return JsonUtils.toJson(stats);
    }

    public Map<String, Object> debug(Long taskId, String keyword) {
        Map<String, Object> result = new HashMap<>();
        result.put("videoInfo", videoTaskService.detail(null, taskId));
        result.put("status", videoTaskService.status(null, taskId));
        result.put("summary", videoResultService.getSummary(null, taskId));
        result.put("segments", ragService.retrieve(taskId, keyword == null ? "视频" : keyword, 5));
        result.put("keywordHit", ragService.locateByKeyword(taskId, keyword == null ? "Agent" : keyword, 5));
        return result;
    }

    private String generateSuggestion(TaskEventVO errorEvent) {
        String detail = errorEvent.getDetail().toLowerCase();
        if (detail.contains("timeout") || detail.contains("超时")) {
            return "建议检查网络连接或尝试手动重试,可能是第三方服务响应缓慢";
        } else if (detail.contains("format") || detail.contains("格式")) {
            return "建议检查视频文件格式是否支持,可尝试转换为 MP4 格式后重新上传";
        } else if (detail.contains("asr") || detail.contains("转写")) {
            return "ASR 服务调用失败,建议检查音频质量或稍后重试";
        } else if (detail.contains("summar") || detail.contains("摘要")) {
            return "AI 摘要生成失败,可能是文本过长或 API 限流,建议稍后重试";
        } else if (detail.contains("upload") || detail.contains("上传")) {
            return "文件上传失败,建议检查网络连接或文件大小限制";
        } else if (detail.contains("ffmpeg") || detail.contains("音频提取")) {
            return "音频提取失败,可能是视频编码不支持,建议转换格式后重试";
        }
        return "建议查看完整事件日志,或联系技术支持";
    }
}
