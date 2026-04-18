package com.videoai.service;

public interface TaskEventLogService {

    void log(Long taskId, String taskNo, String fromStatus, String toStatus, String step, String eventType,
             boolean success, String detail);
}
