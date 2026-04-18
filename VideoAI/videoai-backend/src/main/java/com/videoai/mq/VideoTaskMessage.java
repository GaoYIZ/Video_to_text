package com.videoai.mq;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoTaskMessage {

    private Long taskId;

    private String taskNo;

    private Long fileId;

    private String fileMd5;

    private Long userId;

    private String step;

    private Long timestamp;
}
