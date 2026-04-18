package com.videoai.ai.client;

import com.videoai.ai.model.QuestionAnswerResult;
import com.videoai.ai.model.SummaryResult;

import java.util.List;

public interface DeepSeekClient {

    SummaryResult summarize(String transcriptText);

    QuestionAnswerResult answer(String systemPrompt, String userPrompt, List<String> contextSegments);
}
