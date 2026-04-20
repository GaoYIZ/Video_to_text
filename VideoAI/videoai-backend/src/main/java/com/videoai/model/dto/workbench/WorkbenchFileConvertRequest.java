package com.videoai.model.dto.workbench;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkbenchFileConvertRequest {

    @NotBlank
    private String uploadId;

    private String sessionId;
}
