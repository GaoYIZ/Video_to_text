package com.videoai.service;

import com.videoai.model.dto.chat.ChatAskRequest;
import com.videoai.model.vo.ChatAnswerVO;
import com.videoai.model.vo.PageVO;

public interface ChatService {

    ChatAnswerVO ask(Long userId, ChatAskRequest request);

    PageVO<String> history(Long userId, Long taskId, String sessionId);
}
