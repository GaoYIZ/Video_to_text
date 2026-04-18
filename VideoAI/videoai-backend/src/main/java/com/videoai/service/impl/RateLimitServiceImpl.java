package com.videoai.service.impl;

import com.videoai.common.constant.RedisKeyConstants;
import com.videoai.common.enums.ErrorCode;
import com.videoai.common.exception.BusinessException;
import com.videoai.service.RateLimitService;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimitServiceImpl implements RateLimitService {

    private final RedissonClient redissonClient;

    public RateLimitServiceImpl(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void acquire(Long userId, long permits, long rate, long intervalSeconds) {
        String key = RedisKeyConstants.RATE_LIMIT_USER.formatted(userId);
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.trySetRate(RateType.OVERALL, rate, intervalSeconds, RateIntervalUnit.SECONDS);
        boolean success = rateLimiter.tryAcquire(permits, 100, TimeUnit.MILLISECONDS);
        if (!success) {
            throw new BusinessException(ErrorCode.RATE_LIMIT, "请求过于频繁，请稍后再试");
        }
    }
}
