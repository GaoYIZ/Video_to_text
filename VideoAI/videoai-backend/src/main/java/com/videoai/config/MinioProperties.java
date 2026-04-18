package com.videoai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "videoai.minio")
public class MinioProperties {

    private boolean enabled = false;

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String bucket = "videoai";
}
