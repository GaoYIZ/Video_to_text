package com.videoai.agent;

import com.videoai.model.vo.SummaryVO;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(ChatLanguageModel.class)
public class LangChainAgentRunner {

    private final VideoQAAgent videoQAAgent;
    private final TaskAssistantAgent taskAssistantAgent;
    private final SummaryAgent summaryAgent;

    public LangChainAgentRunner(ChatLanguageModel chatLanguageModel, VideoAgentTools videoAgentTools) {
        this.videoQAAgent = AiServices.builder(VideoQAAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(12))
                .tools(videoAgentTools)
                .build();
        this.taskAssistantAgent = AiServices.builder(TaskAssistantAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(videoAgentTools)
                .build();
        this.summaryAgent = AiServices.builder(SummaryAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

    public String runVideoQa(String sessionId, Long taskId, String question) {
        return videoQAAgent.chat(sessionId, taskId, question);
    }

    public String explainTask(Long taskId, String question) {
        return taskAssistantAgent.explain(taskId, question);
    }

    public SummaryVO generateSummary(String transcript) {
        return summaryAgent.generateSummary(transcript);
    }
}
