package com.videoai.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.videoai.ai.model.AsrResult;
import com.videoai.ai.model.SummaryResult;
import com.videoai.common.constant.RedisKeyConstants;
import com.videoai.common.enums.ErrorCode;
import com.videoai.common.exception.BusinessException;
import com.videoai.common.util.IdUtils;
import com.videoai.common.util.JsonUtils;
import com.videoai.config.VideoAiProperties;
import com.videoai.mapper.MediaFileMapper;
import com.videoai.mapper.TaskEventLogMapper;
import com.videoai.mapper.VideoSummaryMapper;
import com.videoai.mapper.VideoTaskMapper;
import com.videoai.mapper.VideoTranscriptMapper;
import com.videoai.mapper.VideoTranscriptSegmentMapper;
import com.videoai.model.dto.task.CreateVideoTaskRequest;
import com.videoai.model.dto.task.VideoTaskPageRequest;
import com.videoai.model.entity.MediaFile;
import com.videoai.model.entity.TaskEventLog;
import com.videoai.model.entity.UploadRecord;
import com.videoai.model.entity.VideoSummary;
import com.videoai.model.entity.VideoTask;
import com.videoai.model.entity.VideoTranscript;
import com.videoai.model.entity.VideoTranscriptSegment;
import com.videoai.model.enums.TaskStatusEnum;
import com.videoai.model.vo.PageVO;
import com.videoai.model.vo.TaskEventVO;
import com.videoai.model.vo.TaskStatusVO;
import com.videoai.model.vo.VideoTaskDetailVO;
import com.videoai.model.vo.VideoTaskVO;
import com.videoai.mq.VideoTaskDispatchService;
import com.videoai.service.AsrService;
import com.videoai.service.RagService;
import com.videoai.service.SummaryService;
import com.videoai.service.TaskEventLogService;
import com.videoai.service.UploadService;
import com.videoai.service.VideoTaskService;
import com.videoai.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class VideoTaskServiceImpl implements VideoTaskService {

    private final VideoTaskMapper videoTaskMapper;
    private final MediaFileMapper mediaFileMapper;
    private final UploadService uploadService;
    private final VideoTaskDispatchService dispatchService;
    private final TaskEventLogService taskEventLogService;
    private final TaskEventLogMapper taskEventLogMapper;
    private final StorageService storageService;
    private final VideoAiProperties properties;
    private final AsrService asrService;
    private final SummaryService summaryService;
    private final RagService ragService;
    private final VideoTranscriptMapper transcriptMapper;
    private final VideoTranscriptSegmentMapper segmentMapper;
    private final VideoSummaryMapper summaryMapper;
    private final RedissonClient redissonClient;

    public VideoTaskServiceImpl(VideoTaskMapper videoTaskMapper, MediaFileMapper mediaFileMapper, UploadService uploadService,
                                VideoTaskDispatchService dispatchService, TaskEventLogService taskEventLogService,
                                TaskEventLogMapper taskEventLogMapper, StorageService storageService,
                                VideoAiProperties properties, AsrService asrService, SummaryService summaryService,
                                RagService ragService, VideoTranscriptMapper transcriptMapper,
                                VideoTranscriptSegmentMapper segmentMapper, VideoSummaryMapper summaryMapper,
                                RedissonClient redissonClient) {
        this.videoTaskMapper = videoTaskMapper;
        this.mediaFileMapper = mediaFileMapper;
        this.uploadService = uploadService;
        this.dispatchService = dispatchService;
        this.taskEventLogService = taskEventLogService;
        this.taskEventLogMapper = taskEventLogMapper;
        this.storageService = storageService;
        this.properties = properties;
        this.asrService = asrService;
        this.summaryService = summaryService;
        this.ragService = ragService;
        this.transcriptMapper = transcriptMapper;
        this.segmentMapper = segmentMapper;
        this.summaryMapper = summaryMapper;
        this.redissonClient = redissonClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VideoTaskVO createTask(Long userId, CreateVideoTaskRequest request) {
        UploadRecord uploadRecord = uploadService.getByUploadId(request.getUploadId());
        if (!userId.equals(uploadRecord.getUserId()) || uploadRecord.getMediaFileId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请先完成文件上传");
        }

        VideoTask exists = videoTaskMapper.selectOne(Wrappers.<VideoTask>lambdaQuery()
                .eq(VideoTask::getUserId, userId)
                .eq(VideoTask::getFileId, uploadRecord.getMediaFileId())
                .ne(VideoTask::getStatus, TaskStatusEnum.FAILED.getStatus())
                .orderByDesc(VideoTask::getId)
                .last("limit 1"));
        if (exists != null) {
            return toVO(exists);
        }

        MediaFile mediaFile = mediaFileMapper.selectById(uploadRecord.getMediaFileId());
        VideoTask task = new VideoTask();
        task.setTaskNo(IdUtils.taskNo());
        task.setUserId(userId);
        task.setFileId(uploadRecord.getMediaFileId());
        task.setFileMd5(uploadRecord.getFileMd5());
        task.setVideoTitle(mediaFile.getFileName());
        task.setStatus(TaskStatusEnum.UPLOADED.getStatus());
        task.setCurrentStep("UPLOADED");
        task.setProgressPercent(5);
        task.setRetryCount(0);
        task.setSessionId(request.getSessionId());
        videoTaskMapper.insert(task);

        taskEventLogService.log(task.getId(), task.getTaskNo(), null, TaskStatusEnum.UPLOADED.getCode(),
                "UPLOAD_FINISHED", "TASK_CREATED", true, "任务已创建，等待调度执行");
        dispatchService.dispatch(task);
        return toVO(task);
    }

    @Override
    public PageVO<VideoTaskVO> page(Long userId, VideoTaskPageRequest request) {
        IPage<VideoTask> page = videoTaskMapper.selectPage(new Page<>(request.getCurrent(), request.getPageSize()),
                Wrappers.<VideoTask>lambdaQuery()
                        .eq(VideoTask::getUserId, userId)
                        .like(request.getKeyword() != null && !request.getKeyword().isBlank(),
                                VideoTask::getVideoTitle, request.getKeyword())
                        .eq(request.getStatus() != null, VideoTask::getStatus, request.getStatus())
                        .orderByDesc(VideoTask::getId));
        return PageVO.<VideoTaskVO>builder()
                .total(page.getTotal())
                .current(page.getCurrent())
                .pageSize(page.getSize())
                .records(page.getRecords().stream().map(this::toVO).toList())
                .build();
    }

    @Override
    public VideoTaskDetailVO detail(Long userId, Long taskId) {
        VideoTask task = checkTask(taskId, userId);
        return VideoTaskDetailVO.builder()
                .id(task.getId())
                .taskNo(task.getTaskNo())
                .videoTitle(task.getVideoTitle())
                .status(task.getStatus())
                .statusCode(TaskStatusEnum.of(task.getStatus()).getCode())
                .currentStep(task.getCurrentStep())
                .progressPercent(task.getProgressPercent())
                .failReason(task.getFailReason())
                .fileId(task.getFileId())
                .fileMd5(task.getFileMd5())
                .createTime(task.getCreateTime())
                .startedAt(task.getStartedAt())
                .finishedAt(task.getFinishedAt())
                .costTimeMs(task.getCostTimeMs())
                .events(events(userId, taskId))
                .build();
    }

    @Override
    public TaskStatusVO status(Long userId, Long taskId) {
        VideoTask task = checkTask(taskId, userId);
        return TaskStatusVO.builder()
                .taskId(task.getId())
                .status(task.getStatus())
                .statusCode(TaskStatusEnum.of(task.getStatus()).getCode())
                .currentStep(task.getCurrentStep())
                .progressPercent(task.getProgressPercent())
                .failReason(task.getFailReason())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retry(Long userId, Long taskId) {
        VideoTask task = checkTask(taskId, userId);
        task.setRetryCount(task.getRetryCount() + 1);
        task.setFailReason(null);
        task.setStatus(TaskStatusEnum.QUEUED.getStatus());
        task.setCurrentStep("RETRY_QUEUED");
        task.setProgressPercent(10);
        videoTaskMapper.updateById(task);

        taskEventLogService.log(task.getId(), task.getTaskNo(), TaskStatusEnum.FAILED.getCode(), TaskStatusEnum.QUEUED.getCode(),
                "RETRY", "MANUAL_RETRY", true, "用户手动重试任务");
        dispatchService.dispatch(task);
    }

    @Override
    public List<TaskEventVO> events(Long userId, Long taskId) {
        checkTask(taskId, userId);
        return taskEventLogMapper.selectList(Wrappers.<TaskEventLog>lambdaQuery()
                        .eq(TaskEventLog::getTaskId, taskId)
                        .orderByAsc(TaskEventLog::getId))
                .stream()
                .map(event -> TaskEventVO.builder()
                        .fromStatus(event.getFromStatus())
                        .toStatus(event.getToStatus())
                        .step(event.getStep())
                        .eventType(event.getEventType())
                        .success(event.getSuccess())
                        .detail(event.getDetail())
                        .createTime(event.getCreateTime())
                        .build())
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long taskId) {
        VideoTask task = checkTask(taskId, userId);
        videoTaskMapper.deleteById(taskId);
        taskEventLogService.log(taskId, task.getTaskNo(), TaskStatusEnum.of(task.getStatus()).getCode(), "DELETED",
                "DELETE", "TASK_DELETE", true, "任务已删除");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processTask(Long taskId) {
        VideoTask task = videoTaskMapper.selectById(taskId);
        if (task == null) {
            return;
        }

        RLock lock = redissonClient.getLock(RedisKeyConstants.VIDEO_TASK_LOCK.formatted(taskId));
        if (!lock.tryLock()) {
            log.info("skip duplicate task process, taskId={}", taskId);
            return;
        }

        LocalDateTime startTime = LocalDateTime.now();
        try {
            task.setStartedAt(startTime);
            videoTaskMapper.updateById(task);

            transition(task, TaskStatusEnum.QUEUED, "DISPATCH", "任务进入异步处理队列");
            MediaFile mediaFile = mediaFileMapper.selectById(task.getFileId());
            File sourceFile = storageService.downloadToLocal(mediaFile);

            File audioFile = extractAudio(sourceFile, task.getTaskNo());
            transition(task, TaskStatusEnum.PROCESSING_AUDIO, "EXTRACT_AUDIO", "音频提取完成");

            transition(task, TaskStatusEnum.TRANSCRIBING, "ASR", "开始转写视频内容");
            transcriptMapper.delete(Wrappers.<VideoTranscript>lambdaQuery().eq(VideoTranscript::getTaskId, taskId));
            segmentMapper.delete(Wrappers.<VideoTranscriptSegment>lambdaQuery().eq(VideoTranscriptSegment::getTaskId, taskId));
            AsrResult asrResult = asrService.transcribe(task, audioFile);
            VideoTranscript transcript = saveTranscript(task, asrResult);

            transition(task, TaskStatusEnum.SUMMARIZING, "SUMMARY", "开始生成结构化摘要");
            summaryMapper.delete(Wrappers.<VideoSummary>lambdaQuery().eq(VideoSummary::getTaskId, taskId));
            SummaryResult summaryResult = summaryService.summarize(task, transcript);

            transition(task, TaskStatusEnum.INDEXING, "RAG_INDEX", "开始构建转写片段索引");
            buildSegmentIndex(task, transcript, asrResult);

            task.setFinishedAt(LocalDateTime.now());
            task.setCostTimeMs(Duration.between(startTime, task.getFinishedAt()).toMillis());
            videoTaskMapper.updateById(task);
            transition(task, TaskStatusEnum.SUCCESS, "DONE", "任务处理完成，标题：" + summaryResult.getTitle());
        } catch (Exception e) {
            log.error("process task error, taskId={}", taskId, e);
            task.setStatus(TaskStatusEnum.FAILED.getStatus());
            task.setCurrentStep("FAILED");
            task.setFailReason(e.getMessage());
            task.setFinishedAt(LocalDateTime.now());
            task.setCostTimeMs(Duration.between(startTime, task.getFinishedAt()).toMillis());
            videoTaskMapper.updateById(task);
            taskEventLogService.log(task.getId(), task.getTaskNo(), null, TaskStatusEnum.FAILED.getCode(),
                    "FAILED", "PROCESS_ERROR", false, e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void buildSegmentIndex(VideoTask task, VideoTranscript transcript, AsrResult asrResult) {
        List<VideoTranscriptSegment> segments = asrResult.getSegments().stream().map(segment -> {
            VideoTranscriptSegment entity = new VideoTranscriptSegment();
            entity.setTaskId(task.getId());
            entity.setTranscriptId(transcript.getId());
            entity.setSegmentIndex(segment.getIndex());
            entity.setStartTimeMs(segment.getStartTimeMs());
            entity.setEndTimeMs(segment.getEndTimeMs());
            entity.setContent(segment.getContent());
            entity.setKeywords(extractKeywords(segment.getContent()));
            entity.setTokenCount(Math.max(1, segment.getContent().length() / 2));
            return entity;
        }).toList();
        ragService.indexSegments(task.getId(), segments);
    }

    private VideoTranscript saveTranscript(VideoTask task, AsrResult asrResult) {
        VideoTranscript transcript = new VideoTranscript();
        transcript.setTaskId(task.getId());
        transcript.setUserId(task.getUserId());
        transcript.setFileId(task.getFileId());
        transcript.setLanguage(asrResult.getLanguage());
        transcript.setDurationMs(asrResult.getDurationMs());
        transcript.setWordCount(asrResult.getTranscriptText().length());
        transcript.setTranscriptText(asrResult.getTranscriptText());
        transcript.setSegmentJson(JsonUtils.toJson(asrResult.getSegments()));
        transcript.setStatus(1);
        transcriptMapper.insert(transcript);
        return transcript;
    }

    private File extractAudio(File sourceFile, String taskNo) throws IOException, InterruptedException {
        File output = new File(properties.getStorage().getTempBasePath() + "/audio/" + taskNo + ".wav");
        FileUtil.mkdir(output.getParentFile());

        ProcessBuilder processBuilder = new ProcessBuilder(
                properties.getFfmpeg().getCommand(),
                "-y",
                "-i", sourceFile.getAbsolutePath(),
                "-vn",
                "-acodec", "pcm_s16le",
                "-ar", "16000",
                "-ac", "1",
                output.getAbsolutePath()
        );

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0 || !output.exists()) {
                throw new BusinessException("FFmpeg audio extraction failed for task " + taskNo);
            }
            return output;
        } catch (IOException e) {
            throw new BusinessException("FFmpeg audio extraction failed: " + e.getMessage());
        }
    }

    private String extractKeywords(String content) {
        return List.of(content.split("[，。！？；：、\\s]+")).stream()
                .filter(item -> item.length() > 1)
                .limit(5)
                .map(item -> item.toLowerCase(Locale.ROOT))
                .distinct()
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    private void transition(VideoTask task, TaskStatusEnum targetStatus, String step, String detail) {
        String from = TaskStatusEnum.of(task.getStatus()).getCode();
        task.setStatus(targetStatus.getStatus());
        task.setCurrentStep(step);
        task.setProgressPercent(progress(targetStatus));
        videoTaskMapper.updateById(task);
        taskEventLogService.log(task.getId(), task.getTaskNo(), from, targetStatus.getCode(), step, "STATE_CHANGE", true, detail);
    }

    private int progress(TaskStatusEnum status) {
        return switch (status) {
            case PENDING -> 0;
            case UPLOADED -> 5;
            case QUEUED -> 10;
            case PROCESSING_AUDIO -> 30;
            case TRANSCRIBING -> 50;
            case SUMMARIZING -> 75;
            case INDEXING -> 90;
            case SUCCESS -> 100;
            case FAILED -> 0;
        };
    }

    private VideoTask checkTask(Long taskId, Long userId) {
        VideoTask task = videoTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }
        if (userId != null && userId > 0 && !userId.equals(task.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return task;
    }

    private VideoTaskVO toVO(VideoTask task) {
        return VideoTaskVO.builder()
                .id(task.getId())
                .taskNo(task.getTaskNo())
                .videoTitle(task.getVideoTitle())
                .status(task.getStatus())
                .statusCode(TaskStatusEnum.of(task.getStatus()).getCode())
                .currentStep(task.getCurrentStep())
                .progressPercent(task.getProgressPercent())
                .failReason(task.getFailReason())
                .fileId(task.getFileId())
                .createTime(task.getCreateTime())
                .finishedAt(task.getFinishedAt())
                .build();
    }
}
