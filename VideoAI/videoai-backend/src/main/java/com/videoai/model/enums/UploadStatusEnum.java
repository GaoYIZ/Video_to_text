package com.videoai.model.enums;

import lombok.Getter;

@Getter
public enum UploadStatusEnum {

    INIT(0),
    UPLOADING(1),
    MERGED(2),
    FAST_HIT(3);

    private final int code;

    UploadStatusEnum(int code) {
        this.code = code;
    }
}
