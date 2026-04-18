package com.videoai.common.constant;

public final class RedisKeyConstants {

    public static final String UPLOAD_CHUNK = "upload:chunk:%s";
    public static final String UPLOAD_PROGRESS = "upload:progress:%s";
    public static final String VIDEO_TASK_LOCK = "lock:video:task:%s";
    public static final String VIDEO_FILE_LOCK = "lock:video:file:%s";
    public static final String RATE_LIMIT_USER = "rate_limit:user:%s";
    public static final String CHAT_MEMORY = "chat:memory:%s:%s:%s";
    public static final String AI_SUMMARY_CACHE = "ai:summary:cache:%s";
    public static final String AI_QA_CACHE = "ai:qa:cache:%s:%s";
    public static final String MQ_CONSUME_IDEMPOTENT = "mq:consume:idempotent:%s";

    private RedisKeyConstants() {
    }
}
