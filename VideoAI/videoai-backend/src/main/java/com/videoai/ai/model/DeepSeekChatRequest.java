package com.videoai.ai.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DeepSeekChatRequest {

    private String model;

    private List<Map<String, String>> messages;

    private Double temperature;

    private Boolean stream;
}
