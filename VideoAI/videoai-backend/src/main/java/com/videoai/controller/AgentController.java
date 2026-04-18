package com.videoai.controller;

import com.videoai.common.api.BaseResponse;
import com.videoai.common.api.ResultUtils;
import com.videoai.common.context.LoginUserContext;
import com.videoai.model.annotation.LoginRequired;
import com.videoai.model.dto.chat.ChatAskRequest;
import com.videoai.model.vo.ChatAnswerVO;
import com.videoai.service.VideoAgentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@LoginRequired
@RequestMapping("/api/agent/video")
public class AgentController {

    private final VideoAgentService videoAgentService;

    public AgentController(VideoAgentService videoAgentService) {
        this.videoAgentService = videoAgentService;
    }

    @PostMapping("/ask")
    public BaseResponse<ChatAnswerVO> ask(@Valid @RequestBody ChatAskRequest request) {
        return ResultUtils.success(videoAgentService.ask(LoginUserContext.getUserId(), request));
    }

    @GetMapping("/tools/debug")
    public BaseResponse<Map<String, Object>> debug(@RequestParam Long taskId,
                                                   @RequestParam(required = false) String keyword) {
        return ResultUtils.success(videoAgentService.debugTools(LoginUserContext.getUserId(), taskId, keyword));
    }
}
