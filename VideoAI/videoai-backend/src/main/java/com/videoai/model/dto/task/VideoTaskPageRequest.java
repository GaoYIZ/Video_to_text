package com.videoai.model.dto.task;

import lombok.Data;

@Data
public class VideoTaskPageRequest {

    private long current = 1;

    private long pageSize = 10;

    private Integer status;

    private String keyword;
}
