package com.videoai.service;

import com.videoai.model.enums.AiBizTypeEnum;
import com.videoai.model.vo.UsageOverviewVO;
import com.videoai.model.vo.UserQuotaVO;
import com.videoai.model.vo.UsageStatsVO;

public interface UsageService {

    /**
     * 记录 AI 调用用量
     */
    void recordUsage(Long userId, Long taskId, AiBizTypeEnum bizType, String requestHash, String modelName,
                     Integer promptTokens, Integer completionTokens, Long durationMs, boolean hitCache,
                     boolean success, String errorMessage);

    /**
     * 检查用户配额
     */
    void checkQuota(Long userId);

    /**
     * 获取使用概览
     */
    UsageOverviewVO overview(Long userId);

    /**
     * 获取用户配额信息
     */
    UserQuotaVO getUserQuota(Long userId);

    /**
     * 获取使用统计(按周期)
     */
    UsageStatsVO getUsageStats(Long userId, String period);

    /**
     * 获取今日使用情况
     */
    UsageStatsVO getDailyUsage(Long userId);

    /**
     * 获取本月使用情况
     */
    UsageStatsVO getMonthlyUsage(Long userId);
}
