package com.videoai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "videoai")
public class VideoAiProperties {

    private Storage storage = new Storage();

    private Ffmpeg ffmpeg = new Ffmpeg();

    private Ai ai = new Ai();

    private Asr asr = new Asr();

    private Mq mq = new Mq();

    @Data
    public static class Storage {
        private String localBasePath = "data/storage";
        private String tempBasePath = "data/temp";
    }

    @Data
    public static class Ffmpeg {
        private String command = "ffmpeg";
    }

    @Data
    public static class Ai {
        private boolean mock = false;
        // 阿里云 DashScope (通义千问) - 默认
        private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        private String apiKey = "sk-748be92ad75944e99dbf0ab67d202dc4";
        private String model = "qwen-max-latest";
        private Integer timeoutSeconds = 60;
        private Integer maxRetries = 3;
        // DeepSeek 配置（备用）
        private String deepseekBaseUrl = "https://api.deepseek.com";
        private String deepseekApiKey = "sk-839683abf9dd42cba3ba9bb4e65059d8";
        private String deepseekModel = "deepseek-chat";
    }

    @Data
    public static class Asr {
        private boolean mock = true;
        private Integer maxRetries = 3;
        private String pythonCommand = "python";
        private String scriptPath = "";
        private String modelName = "tiny";
        private String language = "zh";
        private String device = "cpu";
        private String computeType = "int8";
        private String cacheDir = "";
        private Integer beamSize = 5;
    }

    @Data
    public static class Mq {
        private boolean enabled = false;
        private String videoTaskTopic = "video-task-topic";
        private String retryTopic = "video-retry-topic";
    }
}
