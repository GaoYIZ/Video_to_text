package com.videoai.controller;

import com.videoai.common.api.BaseResponse;
import com.videoai.common.api.ResultUtils;
import com.videoai.common.context.LoginUserContext;
import com.videoai.model.annotation.LoginRequired;
import com.videoai.model.dto.task.CreateVideoTaskRequest;
import com.videoai.model.dto.task.VideoTaskPageRequest;
import com.videoai.model.vo.PageVO;
import com.videoai.model.vo.TaskEventVO;
import com.videoai.model.vo.TaskStatusVO;
import com.videoai.model.vo.VideoTaskDetailVO;
import com.videoai.model.vo.VideoTaskVO;
import com.videoai.service.VideoTaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@LoginRequired
@RequestMapping("/api/video-task")
public class VideoTaskController {

    private final VideoTaskService videoTaskService;

    public VideoTaskController(VideoTaskService videoTaskService) {
        this.videoTaskService = videoTaskService;
    }

    @PostMapping("/create")
    public BaseResponse<VideoTaskVO> create(@Valid @RequestBody CreateVideoTaskRequest request) {
        return ResultUtils.success(videoTaskService.createTask(LoginUserContext.getUserId(), request));
    }

    @GetMapping("/page")
    public BaseResponse<PageVO<VideoTaskVO>> page(VideoTaskPageRequest request) {
        return ResultUtils.success(videoTaskService.page(LoginUserContext.getUserId(), request));
    }

    @GetMapping("/{id}")
    public BaseResponse<VideoTaskDetailVO> detail(@PathVariable Long id) {
        return ResultUtils.success(videoTaskService.detail(LoginUserContext.getUserId(), id));
    }

    @GetMapping("/{id}/status")
    public BaseResponse<TaskStatusVO> status(@PathVariable Long id) {
        return ResultUtils.success(videoTaskService.status(LoginUserContext.getUserId(), id));
    }

    @PostMapping("/{id}/retry")
    public BaseResponse<Void> retry(@PathVariable Long id) {
        videoTaskService.retry(LoginUserContext.getUserId(), id);
        return ResultUtils.success();
    }

    @GetMapping("/{id}/events")
    public BaseResponse<List<TaskEventVO>> events(@PathVariable Long id) {
        return ResultUtils.success(videoTaskService.events(LoginUserContext.getUserId(), id));
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Void> delete(@PathVariable Long id) {
        videoTaskService.delete(LoginUserContext.getUserId(), id);
        return ResultUtils.success();
    }
}
