package com.shegami.hr_saas.modules.notifications.dto;

import com.shegami.hr_saas.modules.auth.dto.TenantDto;
import com.shegami.hr_saas.modules.auth.dto.UserDto;
import com.shegami.hr_saas.modules.notifications.enums.EntityType;
import com.shegami.hr_saas.modules.notifications.enums.NotificationStatus;
import com.shegami.hr_saas.modules.notifications.enums.NotificationType;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for {@link com.shegami.hr_saas.modules.notifications.entity.Notification}
 */
@Value
public class NotificationDto implements Serializable {
    TenantDto tenant;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String notificationId;
    UserDto recipient;
    NotificationType notificationType;
    String title;
    String message;
    EntityType entityType;
    String entityId;
    UserDto actor;
    String actorName;
    String actorAvatarUrl;
    NotificationStatus status;
    LocalDateTime readAt;
    LocalDateTime clickedAt;
    Boolean sentInApp;
    Boolean sentViaEmail;
    LocalDateTime emailSentAt;
    Map<String, Object> metadata;
    String actionUrl;
}