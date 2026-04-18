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
        private boolean mock = true;
        private String baseUrl = "https://api.deepseek.com";
        private String apiKey = "";
        private String model = "deepseek-chat";
        private Integer timeoutSeconds = 60;
        private Integer maxRetries = 3;
    }

    @Data
    public static class Asr {
        private boolean mock = true;
        private Integer maxRetries = 3;
    }

    @Data
    public static class Mq {
        private boolean enabled = false;
        private String videoTaskTopic = "video-task-topic";
        private String retryTopic = "video-retry-topic";
    }
}
