package com.shegami.hr_saas.modules.notifications.rabbitmq.consumer;

import com.shegami.hr_saas.config.domain.rabbitMq.RabbitMQConfig;
import com.shegami.hr_saas.modules.notifications.dto.NotificationMessage;
import com.shegami.hr_saas.modules.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consumeNotificationEvent(NotificationMessage message) {
        log.info("[RabbitMQ] Received Notification Event for user ID: {} with type: {}", message.getUserId(), message.getNotificationType());
        try {
            notificationService.processNotification(message);
        } catch (Exception e) {
            log.error("[RabbitMQ] Failed to process notification message: {}", e.getMessage(), e);
            throw e; // throw exception to maybe route to DLQ
        }
    }
}
