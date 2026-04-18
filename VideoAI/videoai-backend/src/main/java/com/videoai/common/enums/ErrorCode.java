package com.videoai.common.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(0, "success"),
    PARAMS_ERROR(40000, "请求参数错误"),
    UNAUTHORIZED(40100, "未登录或登录已过期"),
    FORBIDDEN(40300, "无权限访问"),
    NOT_FOUND(40400, "资源不存在"),
    SYSTEM_ERROR(50000, "系统异常"),
    BUSINESS_ERROR(50010, "业务异常"),
    RATE_LIMIT(50011, "请求过于频繁"),
    DUPLICATE_REQUEST(50012, "重复请求"),
    AI_CALL_FAILED(50020, "AI 调用失败"),
    ASR_CALL_FAILED(50021, "ASR 调用失败"),
    FILE_UPLOAD_ERROR(50030, "文件上传失败"),
    TASK_PROCESS_ERROR(50040, "任务处理失败");

    private final int code;

    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
