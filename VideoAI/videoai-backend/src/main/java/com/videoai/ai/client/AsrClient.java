package com.videoai.ai.client;

import com.videoai.ai.model.AsrResult;

import java.io.File;

public interface AsrClient {

    AsrResult transcribe(File audioFile, String taskNo);
}
