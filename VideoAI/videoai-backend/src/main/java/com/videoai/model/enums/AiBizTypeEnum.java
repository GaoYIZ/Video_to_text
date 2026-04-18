package com.videoai.model.enums;

import lombok.Getter;

@Getter
public enum AiBizTypeEnum {

    SUMMARY("SUMMARY"),
    QA("QA"),
    AGENT_QA("AGENT_QA"),
    TASK_ASSISTANT("TASK_ASSISTANT"),
    ASR("ASR");

    private final String code;

    AiBizTypeEnum(String code) {
        this.code = code;
    }
}
