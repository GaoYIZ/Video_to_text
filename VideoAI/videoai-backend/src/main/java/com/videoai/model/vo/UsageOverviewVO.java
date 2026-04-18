package com.videoai.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsageOverviewVO {

    private Integer totalCalls;

    private Integer totalTokens;

    private Integer summaryCalls;

    private Integer qaCalls;

    private Integer taskCount;

    private Integer quotaLimit;
}
