package com.videoai.model.dto.task;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateVideoTaskRequest {

    @NotBlank(message = "uploadId 不能为空")
    private String uploadId;

    private String sessionId;
}
