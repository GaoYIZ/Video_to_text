package com.videoai.service.impl;

import com.videoai.mapper.TaskEventLogMapper;
import com.videoai.model.entity.TaskEventLog;
import com.videoai.service.TaskEventLogService;
import org.springframework.stereotype.Service;

@Service
public class TaskEventLogServiceImpl implements TaskEventLogService {

    private final TaskEventLogMapper taskEventLogMapper;

    public TaskEventLogServiceImpl(TaskEventLogMapper taskEventLogMapper) {
        this.taskEventLogMapper = taskEventLogMapper;
    }

    @Override
    public void log(Long taskId, String taskNo, String fromStatus, String toStatus, String step, String eventType,
                    boolean success, String detail) {
        TaskEventLog eventLog = new TaskEventLog();
        eventLog.setTaskId(taskId);
        eventLog.setTaskNo(taskNo);
        eventLog.setFromStatus(fromStatus);
        eventLog.setToStatus(toStatus);
        eventLog.setStep(step);
        eventLog.setEventType(eventType);
        eventLog.setSuccess(success ? 1 : 0);
        eventLog.setDetail(detail);
        taskEventLogMapper.insert(eventLog);
    }
}
