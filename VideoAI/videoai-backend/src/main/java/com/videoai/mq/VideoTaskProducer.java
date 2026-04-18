package com.videoai.mq;

import com.videoai.config.VideoAiProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "videoai.mq", name = "enabled", havingValue = "true")
public class VideoTaskProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final VideoAiProperties properties;

    public VideoTaskProducer(RocketMQTemplate rocketMQTemplate, VideoAiProperties properties) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.properties = properties;
    }

    public void send(VideoTaskMessage message) {
        rocketMQTemplate.syncSend(properties.getMq().getVideoTaskTopic(),
                MessageBuilder.withPayload(message)
                        .setHeader("KEYS", message.getTaskNo())
                        .build());
        log.info("send mq message, taskId={}", message.getTaskId());
    }
}
