package com.shegami.hr_saas.modules.notifications.service;

import com.shegami.hr_saas.modules.notifications.dto.NotificationDto;
import com.shegami.hr_saas.modules.notifications.dto.NotificationMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    void processNotification(NotificationMessage message);
    Page<NotificationDto> getUserNotifications(Pageable pageable);
    void markAsRead(String notificationId);
    void markAllAsRead();
}

