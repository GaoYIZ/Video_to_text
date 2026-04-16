package com.video.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AiAgentClient {
    
    @Value("${ai.agent.url:http://localhost:8000}")
    private String aiAgentUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 调用 AI Agent 处理音频(转录+总结)
     */
    public AiProcessResult processAudio(String audioPath) {
        String url = aiAgentUrl + "/api/process";
        
        Map<String, String> request = new HashMap<>();
        request.put("audio_path", audioPath);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        
        try {
            log.info("调用 AI Agent 处理音频: {}", audioPath);
            ResponseEntity<AiResponse> response = restTemplate.postForEntity(
                    url, entity, AiResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                AiResponse aiResponse = response.getBody();
                if (aiResponse.getCode() == 200) {
                    log.info("AI 处理成功");
                    return aiResponse.getData();
                } else {
                    throw new RuntimeException("AI 处理失败: " + aiResponse.getMessage());
                }
            } else {
                throw new RuntimeException("AI 服务响应异常");
            }
            
        } catch (Exception e) {
            log.error("调用 AI Agent 失败", e);
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 仅转录音频
     */
    public String transcribeAudio(String audioPath) {
        String url = aiAgentUrl + "/api/transcribe";
        
        Map<String, String> request = new HashMap<>();
        request.put("audio_path", audioPath);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        
        try {
            log.info("调用 AI Agent 转录音频: {}", audioPath);
            ResponseEntity<AiResponse> response = restTemplate.postForEntity(
                    url, entity, AiResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                AiResponse aiResponse = response.getBody();
                if (aiResponse.getCode() == 200) {
                    return aiResponse.getData().getTranscript();
                } else {
                    throw new RuntimeException("转录失败: " + aiResponse.getMessage());
                }
            } else {
                throw new RuntimeException("AI 服务响应异常");
            }
            
        } catch (Exception e) {
            log.error("调用 AI Agent 转录失败", e);
            throw new RuntimeException("转录服务调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 对文本进行总结
     */
    public String summarizeText(String text) {
        String url = aiAgentUrl + "/api/summarize?text=" + 
                java.net.URLEncoder.encode(text, java.nio.charset.StandardCharsets.UTF_8);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        try {
            log.info("调用 AI Agent 总结文本,长度: {}", text.length());
            ResponseEntity<AiResponse> response = restTemplate.postForEntity(
                    url, entity, AiResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                AiResponse aiResponse = response.getBody();
                if (aiResponse.getCode() == 200) {
                    return aiResponse.getData().getSummary();
                } else {
                    throw new RuntimeException("总结失败: " + aiResponse.getMessage());
                }
            } else {
                throw new RuntimeException("AI 服务响应异常");
            }
            
        } catch (Exception e) {
            log.error("调用 AI Agent 总结失败", e);
            throw new RuntimeException("总结服务调用失败: " + e.getMessage());
        }
    }
    
    // 内部类: AI 响应
    @Data
    public static class AiResponse {
        private int code;
        private String message;
        private AiProcessResult data;
    }
    
    // 内部类: AI 处理结果
    @Data
    public static class AiProcessResult {
        private String transcript;
        private String summary;
    }
}
