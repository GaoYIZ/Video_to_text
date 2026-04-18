package com.videoai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "videoai.jwt")
public class JwtProperties {

    private String secret = "ChangeThisSecretToSecureValue1234567890";

    private long expireSeconds = 86400;
}
