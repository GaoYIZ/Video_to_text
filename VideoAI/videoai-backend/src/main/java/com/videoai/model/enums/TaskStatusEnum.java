package com.videoai.model.enums;

import lombok.Getter;

@Getter
public enum TaskStatusEnum {

    PENDING(0, "PENDING", "待处理"),
    UPLOADED(1, "UPLOADED", "已上传"),
    QUEUED(2, "QUEUED", "已入队"),
    PROCESSING_AUDIO(3, "PROCESSING_AUDIO", "音频处理中"),
    TRANSCRIBING(4, "TRANSCRIBING", "语音转写中"),
    SUMMARIZING(5, "SUMMARIZING", "AI 总结中"),
    INDEXING(6, "INDEXING", "内容索引中"),
    SUCCESS(7, "SUCCESS", "成功"),
    FAILED(8, "FAILED", "失败");

    private final int status;
    private final String code;
    private final String desc;

    TaskStatusEnum(int status, String code, String desc) {
        this.status = status;
        this.code = code;
        this.desc = desc;
    }

    public static TaskStatusEnum of(Integer status) {
        if (status == null) {
            return PENDING;
        }
        for (TaskStatusEnum value : values()) {
            if (value.status == status) {
                return value;
            }
        }
        return PENDING;
    }
}
