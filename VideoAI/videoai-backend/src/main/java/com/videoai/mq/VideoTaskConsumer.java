package com.videoai.mq;

import com.videoai.common.constant.RedisKeyConstants;
import com.videoai.service.VideoTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "videoai.mq", name = "enabled", havingValue = "true")
@RocketMQMessageListener(
        topic = "${videoai.mq.video-task-topic:video-task-topic}",
        consumerGroup = "videoai-task-consumer-group",
        messageModel = MessageModel.CLUSTERING
)
public class VideoTaskConsumer implements RocketMQListener<VideoTaskMessage> {

    private final VideoTaskService videoTaskService;
    private final RedisTemplate<String, Object> redisTemplate;

    public VideoTaskConsumer(VideoTaskService videoTaskService, RedisTemplate<String, Object> redisTemplate) {
        this.videoTaskService = videoTaskService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onMessage(VideoTaskMessage message) {
        String idempotentKey = RedisKeyConstants.MQ_CONSUME_IDEMPOTENT.formatted(message.getTaskNo());
        Boolean first = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", Duration.ofHours(12));
        if (Boolean.FALSE.equals(first)) {
            log.info("skip duplicate consume, taskNo={}", message.getTaskNo());
            return;
        }
        videoTaskService.processTask(message.getTaskId());
    }
}
