package com.videoai.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LangChainConfig {

    @Bean
    @ConditionalOnProperty(prefix = "videoai.ai", name = "mock", havingValue = "false")
    public ChatLanguageModel chatLanguageModel(VideoAiProperties properties) {
        return OpenAiChatModel.builder()
                .baseUrl(properties.getAi().getBaseUrl())
                .apiKey(properties.getAi().getApiKey())
                .modelName(properties.getAi().getModel())
                .timeout(Duration.ofSeconds(properties.getAi().getTimeoutSeconds()))
                .build();
    }
}
