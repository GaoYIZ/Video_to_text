package com.video.controller;

import com.video.entity.VideoInfo;
import com.video.service.VideoService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
@CrossOrigin
public class VideoController {
    
    private final VideoService videoService;
    
    /**
     * 上传视频或音频文件
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "video") String type) {
        Map<String, Object> result = new HashMap<>();
        try {
            VideoInfo videoInfo;
            if ("audio".equalsIgnoreCase(type)) {
                videoInfo = videoService.uploadAudio(file);
            } else {
                videoInfo = videoService.uploadVideo(file);
            }
            
            result.put("code", 200);
            result.put("message", "上传成功");
            result.put("data", videoInfo);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "上传失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 解析视频链接
     */
    @PostMapping("/parse-url")
    public ResponseEntity<Map<String, Object>> parseVideoUrl(@RequestBody UrlRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            VideoInfo videoInfo = videoService.parseVideoUrl(request.getUrl());
            result.put("code", 200);
            result.put("message", "解析成功");
            result.put("data", videoInfo);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "解析失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 转换视频为音频
     */
    @PostMapping("/convert/{id}")
    public ResponseEntity<Map<String, Object>> convertToAudio(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            String audioPath = videoService.convertToAudio(id);
            result.put("code", 200);
            result.put("message", "转换成功");
            result.put("data", audioPath);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "转换失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * AI 处理(转录+总结)
     */
    @PostMapping("/process/{id}")
    public ResponseEntity<Map<String, Object>> processWithAi(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            videoService.processWithAi(id);
            result.put("code", 200);
            result.put("message", "AI 处理成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "AI 处理失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 获取视频信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getVideo(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            VideoInfo videoInfo = videoService.getVideoById(id);
            if (videoInfo == null) {
                result.put("code", 404);
                result.put("message", "视频不存在");
                return ResponseEntity.status(404).body(result);
            }
            result.put("code", 200);
            result.put("data", videoInfo);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 获取历史记录
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = videoService.getHistoryList(page, pageSize);
            result.put("code", 200);
            result.put("data", data);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    @Data
    public static class UrlRequest {
        private String url;
    }
}
