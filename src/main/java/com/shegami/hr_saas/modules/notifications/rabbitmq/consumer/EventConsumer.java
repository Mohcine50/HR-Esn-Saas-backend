package com.shegami.hr_saas.modules.notifications.rabbitmq.consumer;

import com.shegami.hr_saas.config.domain.rabbitMq.RabbitMQConfig;
import com.shegami.hr_saas.modules.notifications.dto.*;
import com.shegami.hr_saas.modules.notifications.service.EmailSenderService;
import com.shegami.hr_saas.modules.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.shegami.hr_saas.shared.constants.EmailConstant.QUEUE_NAME;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {

    private final NotificationService notificationService;
    private final EmailSenderService emailService;

    // ==================== IN-APP NOTIFICATIONS ====================

    /**
     * Listen to notification queue
     * Processes in-app notifications and sends via WebSocket
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consumeNotification(NotificationMessage message) {
        log.info("Consuming notification message: {} for user: {}",
                message.getNotificationType(), message.getUserId());

        try {
            // Process notification (save to DB, send via WebSocket)
            notificationService.processNotification(message);

            log.info("Notification processed successfully: {}", message.getMessageId());
        } catch (Exception e) {
            log.error("Failed to process notification message: {}", message.getMessageId(), e);

            // Increment retry count
            message.setRetryCount(message.getRetryCount() + 1);

            // If max retries reached, message will go to DLQ automatically
            if (message.getRetryCount() >= 3) {
                log.error("Max retries reached for notification: {}, sending to DLQ",
                        message.getMessageId());
            }

            throw e; // Re-throw to trigger DLQ
        }
    }

    // ==================== EMAIL INVITATIONS ====================

    /**
     * Listen to invitation email queue
     * Sends invitation emails using Thymeleaf templates
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_INVITATION_QUEUE)
    public void consumeInvitationEmail(EmailInvitationMessage message) {
        log.info("Consuming invitation email message for: {}", message.getRecipientEmail());

        try {
            // Send invitation email
            emailService.sendInvitationEmail(
                    message.getRecipientEmail(),
                    message.getRecipientFirstName(),
                    message.getInviterName(),
                    message.getInvitationToken(),
                    message.getRole(),
                    message.getCompanyName(),
                    message.getMetadata()
            );

            log.info("Invitation email sent successfully to: {}", message.getRecipientEmail());
        } catch (Exception e) {
            log.error("Failed to send invitation email to: {}", message.getRecipientEmail(), e);

            message.setRetryCount(message.getRetryCount() + 1);

            if (message.getRetryCount() >= 3) {
                log.error("Max retries reached for invitation email: {}, sending to DLQ",
                        message.getMessageId());
            }

            throw e;
        }
    }

    // ==================== EMAIL VERIFICATIONS ====================

    /**
     * Listen to verification email queue
     * Sends email verification and password reset emails
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_VERIFICATION_QUEUE)
    public void consumeVerificationEmail(EmailVerificationMessage message) {
        log.info("Consuming verification email message for: {} (type: {})",
                message.getRecipientEmail(), message.getVerificationType());

        try {

            switch (message.getVerificationType()){
                case EMAIL_VERIFICATION -> emailService.sendEmailVerification(
                        message.getRecipientEmail(),
                        message.getRecipientFirstName(),
                        message.getVerificationToken(),
                        message.getCompanyName()
                );
                case PASSWORD_RESET -> emailService.sendPasswordResetEmail(
                        message.getRecipientEmail(),
                        message.getRecipientFirstName(),
                        message.getVerificationToken()
                );
            }

            log.info("Verification email sent successfully to: {}", message.getRecipientEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", message.getRecipientEmail(), e);

            message.setRetryCount(message.getRetryCount() + 1);

            if (message.getRetryCount() >= 3) {
                log.error("Max retries reached for verification email: {}, sending to DLQ",
                        message.getMessageId());
            }

            throw e;
        }
    }

    // ==================== CRITICAL EMAILS ====================

    /**
     * Listen to critical email queue
     * Handles high-priority emails with priority processing
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_CRITICAL_QUEUE)
    public void consumeCriticalEmail(EmailCriticalMessage message) {
        log.info("Consuming CRITICAL email message for: {} (type: {}, priority: {})",
                message.getRecipientEmail(), message.getCriticalType(), message.getPriority());

        try {
            switch (message.getCriticalType()) {
                case "PASSWORD_RESET":
                    emailService.sendPasswordResetEmail(
                            message.getRecipientEmail(),
                            message.getRecipientFirstName(),
                            message.getToken()
                    );
                    break;

                case "SECURITY_ALERT":
                    emailService.sendSecurityAlertEmail(
                            message.getRecipientEmail(),
                            message.getRecipientFirstName(),
                            message.getContext()
                    );
                    break;

                default:
                    log.warn("Unknown critical email type: {}", message.getCriticalType());
            }

            log.info("Critical email sent successfully to: {}", message.getRecipientEmail());
        } catch (Exception e) {
            log.error("Failed to send critical email to: {}", message.getRecipientEmail(), e);

            message.setRetryCount(message.getRetryCount() + 1);

            // Critical emails: retry more aggressively (5 times)
            if (message.getRetryCount() >= 5) {
                log.error("Max retries reached for critical email: {}, sending to DLQ",
                        message.getMessageId());

                // TODO: Alert operations team about failed critical email
                notifyOperationsTeam(message);
            }

            throw e;
        }
    }

    // ==================== DEAD LETTER QUEUE CONSUMERS ====================

    /**
     * Listen to notification DLQ
     * Log and alert about failed notifications
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_DLQ_QUEUE)
    public void consumeNotificationDlq(NotificationMessage message) {
        log.error("NOTIFICATION DLQ: Message failed after max retries: {}", message.getMessageId());
        log.error("Failed notification details - User: {}, Type: {}",
                message.getUserId(), message.getNotificationType());

        // TODO: Store in failed_messages table for manual retry
        // TODO: Send alert to operations team
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_INVITATION_DLQ_QUEUE)
    public void consumeInvitationEmailDlq(EmailInvitationMessage message) {
        log.error("EMAIL INVITATION DLQ: Message failed after max retries: {}", message.getMessageId());
        log.error("Failed invitation email - Recipient: {}, Invitation: {}",
                message.getRecipientEmail(), message.getInvitationId());

        // TODO: Mark invitation as EMAIL_FAILED in database
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_VERIFICATION_DLQ_QUEUE)
    public void consumeVerificationEmailDlq(EmailVerificationMessage message) {
        log.error("EMAIL VERIFICATION DLQ: Message failed after max retries: {}", message.getMessageId());
        log.error("Failed verification email - Recipient: {}, Type: {}",
                message.getRecipientEmail(), message.getVerificationType());
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_CRITICAL_DLQ_QUEUE)
    public void consumeCriticalEmailDlq(EmailCriticalMessage message) {
        log.error("CRITICAL EMAIL DLQ: Message failed after max retries: {}", message.getMessageId());
        log.error("Failed critical email - Recipient: {}, Type: {}, Priority: {}",
                message.getRecipientEmail(), message.getCriticalType(), message.getPriority());

        // TODO: URGENT ALERT - Critical emails must not fail silently
        notifyOperationsTeam(message);
    }

    private void notifyOperationsTeam(EmailCriticalMessage message) {
        log.error("OPERATIONS ALERT: Critical email delivery failed for user: {}",
                message.getUserId());
    }
}