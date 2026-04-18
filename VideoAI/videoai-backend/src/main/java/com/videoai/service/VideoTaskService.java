package com.videoai.service;

import com.videoai.model.dto.task.CreateVideoTaskRequest;
import com.videoai.model.dto.task.VideoTaskPageRequest;
import com.videoai.model.vo.PageVO;
import com.videoai.model.vo.TaskEventVO;
import com.videoai.model.vo.TaskStatusVO;
import com.videoai.model.vo.VideoTaskDetailVO;
import com.videoai.model.vo.VideoTaskVO;

import java.util.List;

public interface VideoTaskService {

    VideoTaskVO createTask(Long userId, CreateVideoTaskRequest request);

    PageVO<VideoTaskVO> page(Long userId, VideoTaskPageRequest request);

    VideoTaskDetailVO detail(Long userId, Long taskId);

    TaskStatusVO status(Long userId, Long taskId);

    void retry(Long userId, Long taskId);

    List<TaskEventVO> events(Long userId, Long taskId);

    void delete(Long userId, Long taskId);

    void processTask(Long taskId);
}
