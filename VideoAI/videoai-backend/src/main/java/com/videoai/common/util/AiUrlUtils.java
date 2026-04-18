package com.videoai.common.util;

public final class AiUrlUtils {

    private AiUrlUtils() {
    }

    public static String normalizeCompatibleBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }
        String normalized = baseUrl.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (!normalized.endsWith("/v1")) {
            normalized += "/v1";
        }
        return normalized;
    }
}
