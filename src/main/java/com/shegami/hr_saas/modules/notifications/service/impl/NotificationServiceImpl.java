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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;

    // SSE Emitters map to track connected clients
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe() {
        String userId = UserContextHolder.getCurrentUserContext().userId();
        log.info("[Notification API] Client subscribing to SSE | userId={}", userId);
        // Timeout set to 30 minutes
        SseEmitter emitter = new SseEmitter(1800000L);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            log.debug("[Notification SSE] Connection completed for user {}", userId);
            emitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            log.debug("[Notification SSE] Connection timeout for user {}", userId);
            emitters.remove(userId);
        });
        emitter.onError((e) -> {
            log.error("[Notification SSE] Connection error for user {}: {}", userId, e.getMessage());
            emitters.remove(userId);
        });

        // Send a dummy event to initialize connection
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected to Notification Stream"));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        return emitter;
    }

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
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("NOTIFICATION")
                        .data(dto));
                log.debug("[Notification SSE] Successfully pushed notification to user {}", userId);
            } catch (IOException e) {
                log.error("[Notification SSE] Failed to send notification to user {}", userId, e);
                emitters.remove(userId);
            }
        }
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
