package com.videoai.config;

import com.videoai.common.util.AiUrlUtils;
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
        VideoAiProperties.Ai aiConfig = properties.getAi();
        
        // 默认使用阿里云 DashScope (通义千问)
        String baseUrl = AiUrlUtils.normalizeCompatibleBaseUrl(aiConfig.getBaseUrl());
        String apiKey = aiConfig.getApiKey();
        String model = aiConfig.getModel();
        
        // 如果需要通过环境变量切换到 DeepSeek，可以设置 VIDEOAI_AI_USE_DEEPSEEK=true
        String useDeepseek = System.getenv("VIDEOAI_AI_USE_DEEPSEEK");
        if ("true".equalsIgnoreCase(useDeepseek)) {
            baseUrl = AiUrlUtils.normalizeCompatibleBaseUrl(aiConfig.getDeepseekBaseUrl());
            apiKey = aiConfig.getDeepseekApiKey();
            model = aiConfig.getDeepseekModel();
        }
        
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(model)
                .timeout(Duration.ofSeconds(aiConfig.getTimeoutSeconds()))
                .build();
    }
}
