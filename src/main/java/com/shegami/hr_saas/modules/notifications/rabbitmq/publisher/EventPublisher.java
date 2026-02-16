package com.shegami.hr_saas.modules.notifications.rabbitmq.publisher;

import com.shegami.hr_saas.config.domain.rabbitMq.RabbitMQConfig;
import com.shegami.hr_saas.modules.auth.entity.SecurityToken;
import com.shegami.hr_saas.modules.auth.service.SecurityTokenService;
import com.shegami.hr_saas.modules.notifications.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final SecurityTokenService securityTokenService;

    public void publishNotification(NotificationMessage message) {
        try {
            message.setMessageId(UUID.randomUUID().toString());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    "notification.created",
                    message
            );

            log.info("Published notification message: {} to user: {}",
                    message.getNotificationType(), message.getUserId());
        } catch (Exception e) {
            log.error("Failed to publish notification message", e);
            throw new RuntimeException("Failed to publish notification", e);
        }
    }


    public void publishInvitationEmail(EmailInvitationMessage message) {
        try {
            message.setMessageId(UUID.randomUUID().toString());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_INVITATION_EXCHANGE,
                    "email.invitation.send",
                    message
            );

            log.info("Published invitation email message for: {}", message.getRecipientEmail());
        } catch (Exception e) {
            log.error("Failed to publish invitation email message", e);
            throw new RuntimeException("Failed to publish invitation email", e);
        }
    }

    public void publishVerificationEmail(EmailVerificationMessage message) {
        try {
            message.setMessageId(UUID.randomUUID().toString());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_VERIFICATION_EXCHANGE,
                    "email.verification.send",
                    message
            );

            log.info("Published verification email message for: {}", message.getRecipientEmail());
        } catch (Exception e) {
            log.error("Failed to publish verification email message", e);
            throw new RuntimeException("Failed to publish verification email", e);
        }
    }


    public void publishCriticalEmail(EmailCriticalMessage message) {
        try {
            message.setMessageId(UUID.randomUUID().toString());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_CRITICAL_EXCHANGE,
                    "email.critical.send",
                    message,
                    messagePostProcessor -> {
                        messagePostProcessor.getMessageProperties()
                                .setPriority(message.getPriority());
                        return messagePostProcessor;
                    }
            );

            log.info("Published critical email message for: {} with priority: {}",
                    message.getRecipientEmail(), message.getPriority());
        } catch (Exception e) {
            log.error("Failed to publish critical email message", e);
            throw new RuntimeException("Failed to publish critical email", e);
        }
    }

    public void sendInvitationEmail(VerificationEmailEventDto build) {
    }
}