package com.videoai.model.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatAskRequest {

    @NotNull(message = "taskId 不能为空")
    private Long taskId;

    @NotBlank(message = "问题不能为空")
    private String question;

    private String sessionId;
}
