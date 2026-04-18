package com.videoai.service;

import com.videoai.ai.model.AsrResult;
import com.videoai.model.entity.VideoTask;

import java.io.File;

public interface AsrService {

    AsrResult transcribe(VideoTask task, File audioFile);
}
