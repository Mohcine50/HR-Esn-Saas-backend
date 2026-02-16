package com.shegami.hr_saas.modules.notifications.service;

import com.shegami.hr_saas.modules.notifications.dto.NotificationMessage;

public interface NotificationService {
    void processNotification(NotificationMessage message);
}
