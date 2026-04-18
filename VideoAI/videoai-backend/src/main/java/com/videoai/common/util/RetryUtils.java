package com.videoai.common.util;

import java.util.concurrent.Callable;

public final class RetryUtils {

    private RetryUtils() {
    }

    public static <T> T execute(Callable<T> callable, int maxRetries, long initialSleepMs) {
        long sleepMs = initialSleepMs;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return callable.call();
            } catch (Exception e) {
                if (attempt == maxRetries) {
                    throw new IllegalStateException(e);
                }
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(interruptedException);
                }
                sleepMs *= 2;
            }
        }
        throw new IllegalStateException("retry execution failed");
    }
}
