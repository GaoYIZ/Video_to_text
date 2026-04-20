package com.videoai.model.dto.workbench;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkbenchAiActionRequest {

    @NotNull
    private Long taskId;

    @NotBlank
    private String action;

    private String question;

    private String sessionId;
}
