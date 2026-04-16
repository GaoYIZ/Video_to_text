package com.video.controller;

import com.video.entity.VideoInfo;
import com.video.service.VideoService;
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
     * 上传视频
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadVideo(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            VideoInfo videoInfo = videoService.uploadVideo(file);
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
}
