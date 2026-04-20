package com.videoai.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkbenchConvertVO {

    private Boolean accepted;

    private String message;

    private VideoTaskVO task;
}
