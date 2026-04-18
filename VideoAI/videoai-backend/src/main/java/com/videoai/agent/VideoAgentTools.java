package com.videoai.agent;

import com.videoai.common.util.JsonUtils;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Tool("Query video basic info for a task")
    public String getVideoBasicInfo(Long taskId) {
        VideoTaskDetailVO detail = videoTaskService.detail(null, taskId);
        return JsonUtils.toJson(detail);
    }

    @Tool("Query current task status and progress")
    public String getTaskStatus(Long taskId) {
        TaskStatusVO status = videoTaskService.status(null, taskId);
        return JsonUtils.toJson(status);
    }

    @Tool("Query task event timeline")
    public String getTaskEvents(Long taskId) {
        return JsonUtils.toJson(videoTaskService.events(null, taskId));
    }

    @Tool("Query full transcript text")
    public String getTranscript(Long taskId) {
        TranscriptVO transcript = videoResultService.getTranscript(null, taskId);
        return transcript == null ? "" : transcript.getTranscriptText();
    }

    @Tool("Query structured AI summary")
    public String getSummary(Long taskId) {
        SummaryVO summary = videoResultService.getSummary(null, taskId);
        return JsonUtils.toJson(summary);
    }

    @Tool("Search relevant transcript segments for the question")
    public String searchSegments(Long taskId, String question) {
        return JsonUtils.toJson(ragService.hybridRetrieve(taskId, question, 5));
    }

    @Tool("Locate transcript segments by keyword")
    public String locateByKeyword(Long taskId, String keyword) {
        return JsonUtils.toJson(ragService.locateByKeyword(taskId, keyword, 5));
    }

    @Tool("Diagnose task failure reason and provide suggestions")
    public String getTaskFailureReason(Long taskId) {
        VideoTaskDetailVO detail = videoTaskService.detail(null, taskId);
        if (detail == null) {
            return "Task not found.";
        }
        if (!"FAILED".equals(detail.getStatusCode())) {
            return "Task is not failed. Current status: " + detail.getStatusCode();
        }

        List<TaskEventVO> events = videoTaskService.events(null, taskId);
        TaskEventVO latestFailure = events.stream()
                .filter(event -> event.getSuccess() != null && event.getSuccess() == 0)
                .reduce((first, second) -> second)
                .orElse(null);
        if (latestFailure == null) {
            return "Task failed, but no explicit failure event was found.";
        }

        Map<String, Object> diagnosis = new LinkedHashMap<>();
        diagnosis.put("step", latestFailure.getStep());
        diagnosis.put("eventType", latestFailure.getEventType());
        diagnosis.put("detail", latestFailure.getDetail());
        diagnosis.put("occurredAt", latestFailure.getCreateTime());
        diagnosis.put("suggestion", buildSuggestion(latestFailure.getDetail()));
        return JsonUtils.toJson(diagnosis);
    }

    @Tool("Analyze task duration statistics by stage")
    public String getTaskDurationStats(Long taskId) {
        List<TaskEventVO> events = videoTaskService.events(null, taskId);
        if (events.size() < 2) {
            return "No enough events for duration analysis.";
        }

        Map<String, Long> stepDurations = new LinkedHashMap<>();
        for (int i = 1; i < events.size(); i++) {
            TaskEventVO previous = events.get(i - 1);
            TaskEventVO current = events.get(i);
            long seconds = Duration.between(previous.getCreateTime(), current.getCreateTime()).toSeconds();
            stepDurations.put(previous.getStep() + " -> " + current.getStep(), seconds);
        }

        long totalSeconds = Duration.between(events.get(0).getCreateTime(), events.get(events.size() - 1).getCreateTime())
                .toSeconds();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSeconds", totalSeconds);
        stats.put("stepDurations", stepDurations);
        return JsonUtils.toJson(stats);
    }

    public Map<String, Object> debug(Long taskId, String keyword) {
        Map<String, Object> result = new HashMap<>();
        result.put("videoInfo", videoTaskService.detail(null, taskId));
        result.put("status", videoTaskService.status(null, taskId));
        result.put("summary", videoResultService.getSummary(null, taskId));
        result.put("segments", ragService.hybridRetrieve(taskId, keyword == null ? "video" : keyword, 5));
        result.put("keywordHit", ragService.locateByKeyword(taskId, keyword == null ? "agent" : keyword, 5));
        result.put("failureDiagnosis", getTaskFailureReason(taskId));
        result.put("durationStats", getTaskDurationStats(taskId));
        return result;
    }

    private String buildSuggestion(String detail) {
        if (detail == null || detail.isBlank()) {
            return "Check task event logs and retry the task if needed.";
        }
        String lower = detail.toLowerCase();
        if (lower.contains("timeout")) {
            return "Possible third-party timeout. Retry later or increase timeout/retry settings.";
        }
        if (lower.contains("format") || lower.contains("codec")) {
            return "The media format may be unsupported. Try converting the video to MP4/H.264 first.";
        }
        if (lower.contains("asr") || lower.contains("transcrib")) {
            return "ASR processing failed. Check audio quality and retry the task.";
        }
        if (lower.contains("summar")) {
            return "Summary generation failed. Check model availability, token limits, or retry later.";
        }
        if (lower.contains("upload")) {
            return "Upload chain may be incomplete. Verify chunk merge result and object storage availability.";
        }
        if (lower.contains("ffmpeg") || lower.contains("audio")) {
            return "Audio extraction failed. Check FFmpeg path and source video encoding.";
        }
        return "Check the latest task event details and retry the task if the dependency is temporarily unavailable.";
    }
}
