package com.videoai.service;

import com.videoai.model.dto.chat.ChatAskRequest;
import com.videoai.model.vo.ChatAnswerVO;

import java.util.Map;

public interface VideoAgentService {

    ChatAnswerVO ask(Long userId, ChatAskRequest request);

    Map<String, Object> debugTools(Long userId, Long taskId, String keyword);
}
