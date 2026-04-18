package com.videoai.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.videoai.common.context.LoginUserContext;
import com.videoai.common.enums.ErrorCode;
import com.videoai.common.exception.BusinessException;
import com.videoai.mapper.AiUsageRecordMapper;
import com.videoai.mapper.UserMapper;
import com.videoai.mapper.VideoTaskMapper;
import com.videoai.model.entity.AiUsageRecord;
import com.videoai.model.entity.User;
import com.videoai.model.enums.AiBizTypeEnum;
import com.videoai.model.vo.UsageOverviewVO;
import com.videoai.model.vo.UserQuotaVO;
import com.videoai.model.vo.UsageStatsVO;
import com.videoai.service.UsageService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UsageServiceImpl implements UsageService {

    private final AiUsageRecordMapper aiUsageRecordMapper;
    private final UserMapper userMapper;
    private final VideoTaskMapper videoTaskMapper;

    public UsageServiceImpl(AiUsageRecordMapper aiUsageRecordMapper, UserMapper userMapper, VideoTaskMapper videoTaskMapper) {
        this.aiUsageRecordMapper = aiUsageRecordMapper;
        this.userMapper = userMapper;
        this.videoTaskMapper = videoTaskMapper;
    }

    @Override
    public void recordUsage(Long userId, Long taskId, AiBizTypeEnum bizType, String requestHash, String modelName,
                            Integer promptTokens, Integer completionTokens, Long durationMs, boolean hitCache,
                            boolean success, String errorMessage) {
        AiUsageRecord record = new AiUsageRecord();
        record.setUserId(userId);
        record.setTaskId(taskId);
        record.setBizType(bizType.getCode());
        record.setRequestHash(requestHash);
        record.setModelName(modelName);
        record.setPromptTokens(promptTokens);
        record.setCompletionTokens(completionTokens);
        record.setTotalTokens((promptTokens == null ? 0 : promptTokens) + (completionTokens == null ? 0 : completionTokens));
        record.setDurationMs(durationMs);
        record.setHitCache(hitCache ? 1 : 0);
        record.setSuccess(success ? 1 : 0);
        record.setErrorMessage(errorMessage);
        aiUsageRecordMapper.insert(record);
    }

    @Override
    public UsageOverviewVO overview(Long userId) {
        User user = userMapper.selectById(userId);
        List<AiUsageRecord> records = aiUsageRecordMapper.selectList(
                Wrappers.<AiUsageRecord>lambdaQuery().eq(AiUsageRecord::getUserId, userId));
        int totalCalls = records.size();
        int totalTokens = records.stream().mapToInt(item -> item.getTotalTokens() == null ? 0 : item.getTotalTokens()).sum();
        int summaryCalls = (int) records.stream().filter(item -> AiBizTypeEnum.SUMMARY.getCode().equals(item.getBizType())).count();
        int qaCalls = (int) records.stream().filter(item ->
                AiBizTypeEnum.QA.getCode().equals(item.getBizType()) || AiBizTypeEnum.AGENT_QA.getCode().equals(item.getBizType())).count();
        Integer taskCount = Math.toIntExact(videoTaskMapper.selectCount(
                Wrappers.<com.videoai.model.entity.VideoTask>lambdaQuery().eq(com.videoai.model.entity.VideoTask::getUserId, userId)));
        return UsageOverviewVO.builder()
                .totalCalls(totalCalls)
                .totalTokens(totalTokens)
                .summaryCalls(summaryCalls)
                .qaCalls(qaCalls)
                .taskCount(taskCount)
                .quotaLimit(user == null ? 0 : user.getQuotaLimit())
                .build();
    }

    @Override
    public void checkQuota(Long userId) {
        Long currentUserId = userId == null ? LoginUserContext.getUserId() : userId;
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        // 检查每日配额
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        long todayCalls = aiUsageRecordMapper.selectCount(
                Wrappers.<AiUsageRecord>lambdaQuery()
                        .eq(AiUsageRecord::getUserId, currentUserId)
                        .ge(AiUsageRecord::getCreateTime, startOfDay)
                        .lt(AiUsageRecord::getCreateTime, endOfDay)
        );
        
        // 默认每日限制 100 次调用
        int dailyLimit = user.getDailyQuotaLimit() != null ? user.getDailyQuotaLimit() : 100;
        if (todayCalls >= dailyLimit) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "今日 AI 调用次数已用尽(" + dailyLimit + "次/天)");
        }
        
        // 检查每月配额
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfMonth = firstDayOfMonth.plusMonths(1).atStartOfDay();
        
        long monthCalls = aiUsageRecordMapper.selectCount(
                Wrappers.<AiUsageRecord>lambdaQuery()
                        .eq(AiUsageRecord::getUserId, currentUserId)
                        .ge(AiUsageRecord::getCreateTime, startOfMonth)
                        .lt(AiUsageRecord::getCreateTime, endOfMonth)
        );
        
        // 默认每月限制 3000 次调用
        int monthlyLimit = user.getMonthlyQuotaLimit() != null ? user.getMonthlyQuotaLimit() : 3000;
        if (monthCalls >= monthlyLimit) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "本月 AI 调用次数已用尽(" + monthlyLimit + "次/月)");
        }
    }

    @Override
    public UserQuotaVO getUserQuota(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        
        // 统计今日使用
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        long todayUsed = aiUsageRecordMapper.selectCount(
                Wrappers.<AiUsageRecord>lambdaQuery()
                        .eq(AiUsageRecord::getUserId, userId)
                        .ge(AiUsageRecord::getCreateTime, startOfDay)
                        .lt(AiUsageRecord::getCreateTime, endOfDay)
        );
        
        // 统计本月使用
        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfMonth = firstDayOfMonth.plusMonths(1).atStartOfDay();
        long monthUsed = aiUsageRecordMapper.selectCount(
                Wrappers.<AiUsageRecord>lambdaQuery()
                        .eq(AiUsageRecord::getUserId, userId)
                        .ge(AiUsageRecord::getCreateTime, startOfMonth)
                        .lt(AiUsageRecord::getCreateTime, endOfMonth)
        );
        
        int dailyLimit = user.getDailyQuotaLimit() != null ? user.getDailyQuotaLimit() : 100;
        int monthlyLimit = user.getMonthlyQuotaLimit() != null ? user.getMonthlyQuotaLimit() : 3000;
        
        return UserQuotaVO.builder()
                .userId(userId)
                .dailyLimit(dailyLimit)
                .monthlyLimit(monthlyLimit)
                .todayUsed((int) todayUsed)
                .monthUsed((int) monthUsed)
                .remainingToday(Math.max(0, dailyLimit - (int) todayUsed))
                .remainingMonth(Math.max(0, monthlyLimit - (int) monthUsed))
                .quotaExceeded(todayUsed >= dailyLimit || monthUsed >= monthlyLimit)
                .build();
    }

    @Override
    public UsageStatsVO getUsageStats(Long userId, String period) {
        if ("day".equals(period)) {
            return getDailyUsage(userId);
        } else if ("month".equals(period)) {
            return getMonthlyUsage(userId);
        }
        return getDailyUsage(userId); // 默认返回今日统计
    }

    @Override
    public UsageStatsVO getDailyUsage(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        List<AiUsageRecord> records = aiUsageRecordMapper.selectList(
                Wrappers.<AiUsageRecord>lambdaQuery()
                        .eq(AiUsageRecord::getUserId, userId)
                        .ge(AiUsageRecord::getCreateTime, startOfDay)
                        .lt(AiUsageRecord::getCreateTime, endOfDay)
        );
        
        return buildUsageStats(userId, "day", records);
    }

    @Override
    public UsageStatsVO getMonthlyUsage(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfMonth = firstDayOfMonth.plusMonths(1).atStartOfDay();
        
        List<AiUsageRecord> records = aiUsageRecordMapper.selectList(
                Wrappers.<AiUsageRecord>lambdaQuery()
                        .eq(AiUsageRecord::getUserId, userId)
                        .ge(AiUsageRecord::getCreateTime, startOfMonth)
                        .lt(AiUsageRecord::getCreateTime, endOfMonth)
        );
        
        return buildUsageStats(userId, "month", records);
    }

    /**
     * 构建使用统计数据
     */
    private UsageStatsVO buildUsageStats(Long userId, String period, List<AiUsageRecord> records) {
        int totalCalls = records.size();
        int totalInputTokens = records.stream().mapToInt(r -> r.getPromptTokens() == null ? 0 : r.getPromptTokens()).sum();
        int totalOutputTokens = records.stream().mapToInt(r -> r.getCompletionTokens() == null ? 0 : r.getCompletionTokens()).sum();
        int totalTokens = totalInputTokens + totalOutputTokens;
        
        // 估算费用(DeepSeek 价格: 输入 0.002元/K tokens, 输出 0.008元/K tokens)
        BigDecimal estimatedCost = BigDecimal.valueOf(totalInputTokens)
                .multiply(BigDecimal.valueOf(0.002))
                .divide(BigDecimal.valueOf(1000), 4, BigDecimal.ROUND_HALF_UP)
                .add(BigDecimal.valueOf(totalOutputTokens)
                        .multiply(BigDecimal.valueOf(0.008))
                        .divide(BigDecimal.valueOf(1000), 4, BigDecimal.ROUND_HALF_UP));
        
        // 平均响应时间
        Long avgResponseTime = records.stream()
                .mapToLong(r -> r.getDurationMs() == null ? 0 : r.getDurationMs())
                .average()
                .orElse(0.0)
                .longValue();
        
        // 缓存命中率
        long cacheHits = records.stream().filter(r -> r.getHitCache() != null && r.getHitCache() == 1).count();
        double cacheHitRate = totalCalls > 0 ? (double) cacheHits / totalCalls * 100 : 0.0;
        
        // 成功率
        long successCount = records.stream().filter(r -> r.getSuccess() != null && r.getSuccess() == 1).count();
        double successRate = totalCalls > 0 ? (double) successCount / totalCalls * 100 : 0.0;
        
        // 按业务类型统计
        Map<String, Integer> callsByBizType = records.stream()
                .collect(Collectors.groupingBy(
                        AiUsageRecord::getBizType,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        // 按模型统计
        Map<String, Integer> callsByModel = records.stream()
                .filter(r -> r.getModelName() != null)
                .collect(Collectors.groupingBy(
                        AiUsageRecord::getModelName,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        return UsageStatsVO.builder()
                .userId(userId)
                .period(period)
                .totalCalls(totalCalls)
                .totalInputTokens(totalInputTokens)
                .totalOutputTokens(totalOutputTokens)
                .totalTokens(totalTokens)
                .estimatedCost(estimatedCost)
                .avgResponseTimeMs(avgResponseTime)
                .cacheHitRate(Math.round(cacheHitRate * 100.0) / 100.0)
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .callsByBizType(callsByBizType)
                .callsByModel(callsByModel)
                .build();
    }
}
