package com.shegami.hr_saas.modules.notifications.service.impl;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.User;
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
        log.info("[Notification] Processing new message for user {}", message.getUserId());

        User user = userRepository.findById(message.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found: " + message.getUserId()));

        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setTenant(user.getTenant());
        notification.setNotificationType(NotificationType.valueOf(message.getNotificationType()));
        notification.setTitle(message.getTitle());
        notification.setMessage(message.getMessage());
        
        if (message.getEntityType() != null) {
            notification.setEntityType(EntityType.valueOf(message.getEntityType()));
        }
        notification.setEntityId(message.getEntityId());

        if (message.getActorId() != null) {
            userRepository.findById(message.getActorId()).ifPresent(notification::setActor);
        }
        notification.setActorName(message.getActorName());
        notification.setMetadata(message.getMetadata());
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setSentInApp(true);

        Notification saved = notificationRepository.save(notification);
        NotificationDto dto = notificationMapper.toDto(saved);

        // Send realtime notification if user is currently connected
        sendToClient(message.getUserId(), dto);
    }

    private void sendToClient(String userId, NotificationDto dto) {
        log.debug("[Notification STOMP] Pushing notification to user {}", userId);
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", dto);
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

        Notification notification = notificationRepository.findByNotificationIdAndRecipient_UserId(notificationId, userId)
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
