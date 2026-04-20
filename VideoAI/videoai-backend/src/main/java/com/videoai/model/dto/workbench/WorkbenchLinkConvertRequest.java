package com.videoai.model.dto.workbench;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkbenchLinkConvertRequest {

    @NotBlank
    private String sourceUrl;

    private String title;
}
