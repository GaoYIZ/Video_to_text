package com.video.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "video")
public class VideoProperties {
    private Upload upload = new Upload();
    private Ffmpeg ffmpeg = new Ffmpeg();
    
    @Data
    public static class Upload {
        private String videoDir;
        private String audioDir;
    }
    
    @Data
    public static class Ffmpeg {
        private String path;
    }
}
