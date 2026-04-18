package com.videoai.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * AI 使用统计 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageStatsVO {
    
    /**
     * 用户 ID
     */
    private Long userId;
    
    /**
     * 统计周期(day/month/year)
     */
    private String period;
    
    /**
     * 总调用次数
     */
    private Integer totalCalls;
    
    /**
     * 总输入 Token 数
     */
    private Integer totalInputTokens;
    
    /**
     * 总输出 Token 数
     */
    private Integer totalOutputTokens;
    
    /**
     * 总 Token 数
     */
    private Integer totalTokens;
    
    /**
     * 预估费用(元)
     */
    private BigDecimal estimatedCost;
    
    /**
     * 平均响应时间(ms)
     */
    private Long avgResponseTimeMs;
    
    /**
     * 缓存命中率(%)
     */
    private Double cacheHitRate;
    
    /**
     * 成功率(%)
     */
    private Double successRate;
    
    /**
     * 按业务类型统计
     */
    private Map<String, Integer> callsByBizType;
    
    /**
     * 按模型统计
     */
    private Map<String, Integer> callsByModel;
}
