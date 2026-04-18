package com.videoai.mq;

import com.videoai.config.VideoAiProperties;
import com.videoai.model.entity.VideoTask;
import com.videoai.service.VideoTaskService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class VideoTaskDispatchService {

    private final VideoAiProperties properties;
    private final VideoTaskProducer videoTaskProducer;
    private final ObjectProvider<VideoTaskService> videoTaskServiceProvider;

    public VideoTaskDispatchService(VideoAiProperties properties, ObjectProvider<VideoTaskProducer> producerProvider,
                                    ObjectProvider<VideoTaskService> videoTaskServiceProvider) {
        this.properties = properties;
        this.videoTaskProducer = producerProvider.getIfAvailable();
        this.videoTaskServiceProvider = videoTaskServiceProvider;
    }

    public void dispatch(VideoTask task) {
        if (properties.getMq().isEnabled() && videoTaskProducer != null) {
            videoTaskProducer.send(VideoTaskMessage.builder()
                    .taskId(task.getId())
                    .taskNo(task.getTaskNo())
                    .fileId(task.getFileId())
                    .fileMd5(task.getFileMd5())
                    .userId(task.getUserId())
                    .step(task.getCurrentStep())
                    .timestamp(System.currentTimeMillis())
                    .build());
            return;
        }
        dispatchLocal(task.getId());
    }

    @Async("videoTaskExecutor")
    public void dispatchLocal(Long taskId) {
        videoTaskServiceProvider.getObject().processTask(taskId);
    }
}
