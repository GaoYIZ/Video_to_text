package com.videoai.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class WorkbenchMonitorVO {

    private Integer totalTasks;

    private Integer processingTasks;

    private Integer successTasks;

    private Integer failedTasks;

    private Map<String, Integer> statusDistribution;

    private UserQuotaVO quota;

    private UsageStatsVO dailyStats;

    private UsageStatsVO monthlyStats;

    private List<VideoTaskVO> recentTasks;

    private List<MonitorEventVO> recentEvents;
}
