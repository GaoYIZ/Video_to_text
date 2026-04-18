package com.videoai.service;

public interface RateLimitService {

    void acquire(Long userId, long permits, long rate, long intervalSeconds);
}
