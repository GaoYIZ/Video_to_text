package com.videoai.agent;

import com.videoai.model.vo.SummaryVO;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agent for structured summary generation from long transcript text.
 */
public interface SummaryAgent {

    @SystemMessage(fromResource = "prompts/summary-agent-system.txt")
    @UserMessage("""
            Transcript:
            {{transcript}}

            Please generate a structured summary in the required format.
            """)
    SummaryVO generateSummary(@V("transcript") String transcript);
}
