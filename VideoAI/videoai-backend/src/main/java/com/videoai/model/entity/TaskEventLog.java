package com.videoai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("task_event_log")
@EqualsAndHashCode(callSuper = true)
public class TaskEventLog extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String taskNo;

    private String fromStatus;

    private String toStatus;

    private String step;

    private String eventType;

    private Integer success;

    private String detail;
}
