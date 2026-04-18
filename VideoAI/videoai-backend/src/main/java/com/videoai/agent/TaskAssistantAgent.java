package com.videoai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface TaskAssistantAgent {

    @SystemMessage(fromResource = "prompts/task-assistant-system.txt")
    @UserMessage("""
            视频任务 ID：{{taskId}}
            用户问题：{{question}}
            请优先使用任务状态与事件日志工具解释任务进度与失败原因。
            """)
    String explain(@V("taskId") Long taskId, @V("question") String question);
}
