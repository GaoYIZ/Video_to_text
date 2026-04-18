package com.videoai.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户配额信息 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQuotaVO {
    
    /**
     * 用户 ID
     */
    private Long userId;
    
    /**
     * 每日调用次数限制
     */
    private Integer dailyLimit;
    
    /**
     * 每月调用次数限制
     */
    private Integer monthlyLimit;
    
    /**
     * 今日已用次数
     */
    private Integer todayUsed;
    
    /**
     * 本月已用次数
     */
    private Integer monthUsed;
    
    /**
     * 今日剩余次数
     */
    private Integer remainingToday;
    
    /**
     * 本月剩余次数
     */
    private Integer remainingMonth;
    
    /**
     * 是否超出配额
     */
    private Boolean quotaExceeded;
}
