package com.video.service.impl;

import com.video.client.AiAgentClient;
import com.video.config.VideoProperties;
import com.video.entity.VideoInfo;
import com.video.mapper.VideoMapper;
import com.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {
    
    private final VideoMapper videoMapper;
    private final VideoProperties videoProperties;
    private final AiAgentClient aiAgentClient;
    
    @Override
    @Transactional
    public VideoInfo uploadVideo(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        try {
            // 创建上传目录
            String videoDir = videoProperties.getUpload().getVideoDir();
            File dir = new File(videoDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".mp4";
            String fileName = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = Paths.get(videoDir, fileName);
            Files.write(filePath, file.getBytes());
            
            log.info("视频上传成功: {}", filePath.toAbsolutePath());
            
            // 保存数据库记录
            VideoInfo videoInfo = new VideoInfo();
            videoInfo.setOriginalName(originalFilename);
            videoInfo.setVideoPath(filePath.toString());
            videoInfo.setStatus("UPLOADED");
            videoInfo.setCreateTime(LocalDateTime.now());
            videoInfo.setUpdateTime(LocalDateTime.now());
            videoInfo.setDeleted(0);
            
            videoMapper.insert(videoInfo);
            
            return videoInfo;
            
        } catch (IOException e) {
            log.error("视频上传失败", e);
            throw new RuntimeException("视频上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public String convertToAudio(Long videoId) {
        VideoInfo videoInfo = videoMapper.selectById(videoId);
        if (videoInfo == null) {
            throw new RuntimeException("视频不存在");
        }
        
        try {
            // 创建音频输出目录
            String audioDir = videoProperties.getUpload().getAudioDir();
            File dir = new File(audioDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成音频文件名
            String audioFileName = UUID.randomUUID().toString() + ".mp3";
            String audioPath = Paths.get(audioDir, audioFileName).toString();
            
            // 执行 FFmpeg 转换
            String ffmpegPath = videoProperties.getFfmpeg().getPath();
            String command = String.format("%s -i \"%s\" -vn -acodec libmp3lame -ab 192k \"%s\"",
                    ffmpegPath,
                    videoInfo.getVideoPath(),
                    audioPath);
            
            log.info("执行 FFmpeg 命令: {}", command);
            
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg 转换失败,退出码: " + exitCode);
            }
            
            // 更新数据库
            videoInfo.setAudioPath(audioPath);
            videoInfo.setStatus("CONVERTED");
            videoInfo.setUpdateTime(LocalDateTime.now());
            videoMapper.updateById(videoInfo);
            
            log.info("音频转换成功: {}", audioPath);
            return audioPath;
            
        } catch (Exception e) {
            log.error("音频转换失败", e);
            throw new RuntimeException("音频转换失败: " + e.getMessage());
        }
    }
    
    @Override
    public VideoInfo getVideoById(Long id) {
        return videoMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public void processWithAi(Long videoId) {
        VideoInfo videoInfo = videoMapper.selectById(videoId);
        if (videoInfo == null) {
            throw new RuntimeException("视频不存在");
        }
        
        if (videoInfo.getAudioPath() == null || videoInfo.getAudioPath().isEmpty()) {
            throw new RuntimeException("音频文件不存在,请先转换音频");
        }
        
        try {
            log.info("开始 AI 处理: videoId={}", videoId);
            
            // 调用 AI Agent 进行转录和总结
            AiAgentClient.AiProcessResult result = aiAgentClient.processAudio(videoInfo.getAudioPath());
            
            // 更新数据库
            videoInfo.setTranscript(result.getTranscript());
            videoInfo.setSummary(result.getSummary());
            videoInfo.setStatus("SUMMARIZED");
            videoInfo.setUpdateTime(LocalDateTime.now());
            videoMapper.updateById(videoInfo);
            
            log.info("AI 处理完成: videoId={}", videoId);
            
        } catch (Exception e) {
            log.error("AI 处理失败: videoId={}", videoId, e);
            throw new RuntimeException("AI 处理失败: " + e.getMessage());
        }
    }
}
    @Override
    @Transactional
    public void processWithAi(Long videoId) {
        VideoInfo videoInfo = videoMapper.selectById(videoId);
        if (videoInfo == null) {
            throw new RuntimeException("视频不存在");
        }
        
        if (videoInfo.getAudioPath() == null || videoInfo.getAudioPath().isEmpty()) {
            throw new RuntimeException("音频文件不存在,请先转换音频");
        }
        
        try {
            log.info("开始 AI 处理: videoId={}", videoId);
            
            // 调用 AI Agent 进行转录和总结
            AiAgentClient.AiProcessResult result = aiAgentClient.processAudio(videoInfo.getAudioPath());
            
            // 更新数据库
            videoInfo.setTranscript(result.getTranscript());
            videoInfo.setSummary(result.getSummary());
            videoInfo.setStatus("SUMMARIZED");
            videoInfo.setUpdateTime(LocalDateTime.now());
            videoMapper.updateById(videoInfo);
            
            log.info("AI 处理完成: videoId={}", videoId);
            
        } catch (Exception e) {
            log.error("AI 处理失败: videoId={}", videoId, e);
            throw new RuntimeException("AI 处理失败: " + e.getMessage());
        }
    }
}
