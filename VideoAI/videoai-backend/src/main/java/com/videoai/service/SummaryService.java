package com.videoai.service;

import com.videoai.ai.model.SummaryResult;
import com.videoai.model.entity.VideoTask;
import com.videoai.model.entity.VideoTranscript;

public interface SummaryService {

    SummaryResult summarize(VideoTask task, VideoTranscript transcript);
}
