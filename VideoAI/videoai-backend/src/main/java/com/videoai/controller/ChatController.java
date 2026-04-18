package com.videoai.controller;

import com.videoai.common.api.BaseResponse;
import com.videoai.common.api.ResultUtils;
import com.videoai.common.context.LoginUserContext;
import com.videoai.model.annotation.LoginRequired;
import com.videoai.model.dto.chat.ChatAskRequest;
import com.videoai.model.vo.ChatAnswerVO;
import com.videoai.model.vo.PageVO;
import com.videoai.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@LoginRequired
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/ask")
    public BaseResponse<ChatAnswerVO> ask(@Valid @RequestBody ChatAskRequest request) {
        return ResultUtils.success(chatService.ask(LoginUserContext.getUserId(), request));
    }

    @GetMapping("/history")
    public BaseResponse<PageVO<String>> history(@RequestParam Long taskId, @RequestParam String sessionId) {
        return ResultUtils.success(chatService.history(LoginUserContext.getUserId(), taskId, sessionId));
    }
}
