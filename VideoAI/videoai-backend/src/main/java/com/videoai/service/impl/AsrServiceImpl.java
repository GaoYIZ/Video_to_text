package com.videoai.service.impl;

import com.videoai.ai.client.AsrClient;
import com.videoai.ai.model.AsrResult;
import com.videoai.common.util.RetryUtils;
import com.videoai.config.VideoAiProperties;
import com.videoai.model.entity.VideoTask;
import com.videoai.service.AsrService;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class AsrServiceImpl implements AsrService {

    private final AsrClient asrClient;
    private final VideoAiProperties properties;

    public AsrServiceImpl(AsrClient asrClient, VideoAiProperties properties) {
        this.asrClient = asrClient;
        this.properties = properties;
    }

    @Override
    public AsrResult transcribe(VideoTask task, File audioFile) {
        return RetryUtils.execute(() -> asrClient.transcribe(audioFile, task.getTaskNo()),
                properties.getAsr().getMaxRetries(), 300L);
    }
}
