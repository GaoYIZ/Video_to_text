package com.videoai.controller;

import com.videoai.common.api.BaseResponse;
import com.videoai.common.api.ResultUtils;
import com.videoai.common.context.LoginUserContext;
import com.videoai.model.annotation.LoginRequired;
import com.videoai.model.vo.SummaryVO;
import com.videoai.model.vo.TranscriptSegmentVO;
import com.videoai.model.vo.TranscriptVO;
import com.videoai.service.VideoResultService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@LoginRequired
@RequestMapping("/api/video-result")
public class VideoResultController {

    private final VideoResultService videoResultService;

    public VideoResultController(VideoResultService videoResultService) {
        this.videoResultService = videoResultService;
    }

    @GetMapping("/{taskId}/transcript")
    public BaseResponse<TranscriptVO> transcript(@PathVariable Long taskId) {
        return ResultUtils.success(videoResultService.getTranscript(LoginUserContext.getUserId(), taskId));
    }

    @GetMapping("/{taskId}/summary")
    public BaseResponse<SummaryVO> summary(@PathVariable Long taskId) {
        return ResultUtils.success(videoResultService.getSummary(LoginUserContext.getUserId(), taskId));
    }

    @GetMapping("/{taskId}/segments")
    public BaseResponse<List<TranscriptSegmentVO>> segments(@PathVariable Long taskId) {
        return ResultUtils.success(videoResultService.getSegments(LoginUserContext.getUserId(), taskId));
    }
}
