package com.shegami.hr_saas.modules.notifications.service;

import com.shegami.hr_saas.modules.notifications.dto.NotificationDto;
import com.shegami.hr_saas.modules.notifications.dto.NotificationMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {
    void processNotification(NotificationMessage message);
    SseEmitter subscribe();
    Page<NotificationDto> getUserNotifications(Pageable pageable);
    void markAsRead(String notificationId);
    void markAllAsRead();
}
