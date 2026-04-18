package com.videoai.agent;

import com.videoai.model.vo.SummaryVO;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 视频摘要智能体
 * 负责对长视频转写文本进行深度分析和结构化总结
 */
public interface SummaryAgent {

    @SystemMessage(fromResource = "prompts/summary-agent-system.txt")
    @UserMessage("""
            视频转写文本如下:
            {{transcript}}
            
            请生成结构化摘要。
            """)
    SummaryVO generateSummary(@V("transcript") String transcript);
}
