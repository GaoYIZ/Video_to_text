package com.videoai.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WorkbenchOverviewVO {

    private UsageOverviewVO usageOverview;

    private UserQuotaVO quota;

    private Integer processingTasks;

    private Integer completedTasks;

    private Integer failedTasks;

    private List<VideoTaskVO> recentTasks;
}
