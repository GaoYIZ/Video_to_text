package com.videoai.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface VideoQAAgent {

    @SystemMessage(fromResource = "prompts/video-qa-system.txt")
    @UserMessage("""
            视频任务 ID：{{taskId}}
            用户问题：{{question}}
            请结合工具调用结果与上下文给出准确回答。
            """)
    String chat(@MemoryId String memoryId, @V("taskId") Long taskId, @V("question") String question);
}
