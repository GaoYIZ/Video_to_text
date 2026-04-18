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
import com.videoai.model.vo.UsageStatsVO;
import com.videoai.model.vo.UserQuotaVO;
import com.videoai.service.UsageService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UsageServiceImpl implements UsageService {

    private static final BigDecimal INPUT_TOKEN_PRICE_PER_K = new BigDecimal("0.002");
    private static final BigDecimal OUTPUT_TOKEN_PRICE_PER_K = new BigDecimal("0.008");

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
    public void checkQuota(Long userId) {
        Long currentUserId = userId == null ? LoginUserContext.getUserId() : userId;
        User user = requireUser(currentUserId);
        UserQuotaVO quota = getUserQuota(user.getId());
        if (quota.getRemainingToday() <= 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "今日 AI 调用次数已达上限");
        }
        if (quota.getRemainingMonth() <= 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "本月 AI 调用次数已达上限");
        }
    }

    @Override
    public UsageOverviewVO overview(Long userId) {
        User user = requireUser(userId);
        List<AiUsageRecord> records = listByUser(userId);
        int totalCalls = records.size();
        int totalTokens = records.stream().mapToInt(item -> safeInt(item.getTotalTokens())).sum();
        int summaryCalls = (int) records.stream()
                .filter(item -> AiBizTypeEnum.SUMMARY.getCode().equals(item.getBizType()))
                .count();
        int qaCalls = (int) records.stream()
                .filter(item -> AiBizTypeEnum.QA.getCode().equals(item.getBizType())
                        || AiBizTypeEnum.AGENT_QA.getCode().equals(item.getBizType()))
                .count();
        int taskCount = Math.toIntExact(videoTaskMapper.selectCount(
                Wrappers.<com.videoai.model.entity.VideoTask>lambdaQuery()
                        .eq(com.videoai.model.entity.VideoTask::getUserId, userId)));
        return UsageOverviewVO.builder()
                .totalCalls(totalCalls)
                .totalTokens(totalTokens)
                .summaryCalls(summaryCalls)
                .qaCalls(qaCalls)
                .taskCount(taskCount)
                .quotaLimit(user.getMonthlyQuotaLimit() == null ? safeInt(user.getQuotaLimit()) : user.getMonthlyQuotaLimit())
                .build();
    }

    @Override
    public UserQuotaVO getUserQuota(Long userId) {
        User user = requireUser(userId);
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);

        long todayUsed = aiUsageRecordMapper.selectCount(Wrappers.<AiUsageRecord>lambdaQuery()
                .eq(AiUsageRecord::getUserId, userId)
                .ge(AiUsageRecord::getCreateTime, today.atStartOfDay())
                .lt(AiUsageRecord::getCreateTime, today.plusDays(1).atStartOfDay()));

        long monthUsed = aiUsageRecordMapper.selectCount(Wrappers.<AiUsageRecord>lambdaQuery()
                .eq(AiUsageRecord::getUserId, userId)
                .ge(AiUsageRecord::getCreateTime, firstDayOfMonth.atStartOfDay())
                .lt(AiUsageRecord::getCreateTime, firstDayOfMonth.plusMonths(1).atStartOfDay()));

        int dailyLimit = user.getDailyQuotaLimit() == null ? 100 : user.getDailyQuotaLimit();
        int monthlyLimit = user.getMonthlyQuotaLimit() == null ? 3000 : user.getMonthlyQuotaLimit();
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
        return "month".equalsIgnoreCase(period) ? getMonthlyUsage(userId) : getDailyUsage(userId);
    }

    @Override
    public UsageStatsVO getDailyUsage(Long userId) {
        LocalDate today = LocalDate.now();
        List<AiUsageRecord> records = aiUsageRecordMapper.selectList(Wrappers.<AiUsageRecord>lambdaQuery()
                .eq(AiUsageRecord::getUserId, userId)
                .ge(AiUsageRecord::getCreateTime, today.atStartOfDay())
                .lt(AiUsageRecord::getCreateTime, today.plusDays(1).atStartOfDay()));
        return buildUsageStats(userId, "day", records);
    }

    @Override
    public UsageStatsVO getMonthlyUsage(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        List<AiUsageRecord> records = aiUsageRecordMapper.selectList(Wrappers.<AiUsageRecord>lambdaQuery()
                .eq(AiUsageRecord::getUserId, userId)
                .ge(AiUsageRecord::getCreateTime, firstDayOfMonth.atStartOfDay())
                .lt(AiUsageRecord::getCreateTime, firstDayOfMonth.plusMonths(1).atStartOfDay()));
        return buildUsageStats(userId, "month", records);
    }

    private UsageStatsVO buildUsageStats(Long userId, String period, List<AiUsageRecord> records) {
        int totalCalls = records.size();
        int totalInputTokens = records.stream().mapToInt(record -> safeInt(record.getPromptTokens())).sum();
        int totalOutputTokens = records.stream().mapToInt(record -> safeInt(record.getCompletionTokens())).sum();
        int totalTokens = totalInputTokens + totalOutputTokens;

        BigDecimal estimatedCost = BigDecimal.valueOf(totalInputTokens)
                .multiply(INPUT_TOKEN_PRICE_PER_K)
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP)
                .add(BigDecimal.valueOf(totalOutputTokens)
                        .multiply(OUTPUT_TOKEN_PRICE_PER_K)
                        .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP));

        long avgResponseTime = Math.round(records.stream()
                .mapToLong(record -> record.getDurationMs() == null ? 0L : record.getDurationMs())
                .average()
                .orElse(0D));

        long cacheHits = records.stream().filter(record -> record.getHitCache() != null && record.getHitCache() == 1).count();
        long successCount = records.stream().filter(record -> record.getSuccess() != null && record.getSuccess() == 1).count();
        double cacheHitRate = totalCalls == 0 ? 0D : cacheHits * 100D / totalCalls;
        double successRate = totalCalls == 0 ? 0D : successCount * 100D / totalCalls;

        Map<String, Integer> callsByBizType = records.stream()
                .collect(Collectors.groupingBy(AiUsageRecord::getBizType,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        Map<String, Integer> callsByModel = records.stream()
                .filter(record -> record.getModelName() != null && !record.getModelName().isBlank())
                .collect(Collectors.groupingBy(AiUsageRecord::getModelName,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

        return UsageStatsVO.builder()
                .userId(userId)
                .period(period)
                .totalCalls(totalCalls)
                .totalInputTokens(totalInputTokens)
                .totalOutputTokens(totalOutputTokens)
                .totalTokens(totalTokens)
                .estimatedCost(estimatedCost)
                .avgResponseTimeMs(avgResponseTime)
                .cacheHitRate(round2(cacheHitRate))
                .successRate(round2(successRate))
                .callsByBizType(callsByBizType)
                .callsByModel(callsByModel)
                .build();
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }

    private List<AiUsageRecord> listByUser(Long userId) {
        return aiUsageRecordMapper.selectList(Wrappers.<AiUsageRecord>lambdaQuery()
                .eq(AiUsageRecord::getUserId, userId));
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double round2(double value) {
        return Math.round(value * 100D) / 100D;
    }
}
