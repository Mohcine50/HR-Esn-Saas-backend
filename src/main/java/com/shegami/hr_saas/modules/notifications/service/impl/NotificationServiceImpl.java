package com.shegami.hr_saas.modules.notifications.service.impl;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.exception.UserNotFoundException;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.notifications.dto.NotificationDto;
import com.shegami.hr_saas.modules.notifications.dto.NotificationMessage;
import com.shegami.hr_saas.modules.notifications.entity.Notification;
import com.shegami.hr_saas.modules.notifications.enums.EntityType;
import com.shegami.hr_saas.modules.notifications.enums.NotificationStatus;
import com.shegami.hr_saas.modules.notifications.enums.NotificationType;
import com.shegami.hr_saas.modules.notifications.mapper.NotificationMapper;
import com.shegami.hr_saas.modules.notifications.repository.NotificationRepository;
import com.shegami.hr_saas.modules.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void processNotification(NotificationMessage message) {
        if (message.getUserId() == null) {
            log.error("[Notification] Rejected: No userId specified in message: {}", message);
            return;
        }

        log.info("[Notification] Processing {} for user {}", message.getNotificationType(), message.getUserId());

        User user = userRepository.findById(message.getUserId())
                .orElseThrow(() -> {
                    log.error("[Notification] Fail: Recipient user {} not found", message.getUserId());
                    return new UserNotFoundException("Recipient not found: " + message.getUserId());
                });

        Notification notification = Notification.builder()
                .recipient(user)
                .notificationType(message.getNotificationType())
                .title(message.getTitle() != null ? message.getTitle()
                        : message.getNotificationType().getDefaultTitle())
                .message(message.getMessage())
                .entityType(message.getEntityType())
                .entityId(message.getEntityId())
                .actorName(message.getActorName())
                .metadata(message.getMetadata())
                .status(NotificationStatus.UNREAD)
                .sentInApp(true)
                .actionUrl(generateActionUrl(message.getEntityType(), message.getEntityId()))
                .build();
        notification.setTenant(user.getTenant());

        if (message.getActorId() != null) {
            userRepository.findById(message.getActorId()).ifPresent(actor -> {
                notification.setActor(actor);
                if (notification.getActorName() == null) {
                    notification.setActorName(actor.getFullName());
                }
            });
        }

        Notification saved = notificationRepository.save(notification);
        NotificationDto dto = notificationMapper.toDto(saved);

        // Send realtime notification if user is currently connected
        sendToClient(message.getUserId(), dto);
    }

    private String generateActionUrl(EntityType type, String id) {
        if (type == null || id == null)
            return null;
        return switch (type) {
            case MISSION -> "/missions/" + id;
            case TIMESHEET -> "/timesheets/" + id;
            case PROJECT -> "/projects/" + id;
            case INVOICE -> "/billing?invoiceId=" + id;
            case USER -> "/users/" + id;
            default -> null;
        };
    }

    private void sendToClient(String userId, NotificationDto dto) {
        log.info("[Notification STOMP] Sending to user: {} | title: {}", userId, dto.getTitle());

        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                dto);

        log.info("[Notification STOMP] Message sent to /user/{}/queue/notifications", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(Pageable pageable) {
        String userId = UserContextHolder.getCurrentUserContext().userId();
        return notificationRepository.findLatestForUser(userId, pageable)
                .map(notificationMapper::toDto);
    }

    @Override
    public void markAsRead(String notificationId) {
        String userId = UserContextHolder.getCurrentUserContext().userId();

        Notification notification = notificationRepository
                .findByNotificationIdAndRecipient_UserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found or access denied"));

        if (notification.getStatus() == NotificationStatus.UNREAD) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("[Notification] Marked notification {} as READ for user {}", notificationId, userId);
        }
    }

    @Override
    public void markAllAsRead() {
        String userId = UserContextHolder.getCurrentUserContext().userId();
        int updated = notificationRepository.markAllAsReadForUser(userId, LocalDateTime.now());
        log.info("[Notification] Marked {} notifications as READ for user {}", updated, userId);
    }
}
