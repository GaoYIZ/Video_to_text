package com.video.service;

import com.video.entity.VideoInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface VideoService {
    /**
     * 上传视频文件
     */
    VideoInfo uploadVideo(MultipartFile file);
    
    /**
     * 上传音频文件
     */
    VideoInfo uploadAudio(MultipartFile file);
    
    /**
     * 解析视频链接
     */
    VideoInfo parseVideoUrl(String url);
    
    /**
     * 将视频转换为音频
     */
    String convertToAudio(Long videoId);
    
    /**
     * 处理音频(转录+总结)
     */
    void processWithAi(Long videoId);
    
    /**
     * 根据ID获取视频信息
     */
    VideoInfo getVideoById(Long id);
    
    /**
     * 获取历史记录
     */
    Map<String, Object> getHistoryList(int page, int pageSize);
}
