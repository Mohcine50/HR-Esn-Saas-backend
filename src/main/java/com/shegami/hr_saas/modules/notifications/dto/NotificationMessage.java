package com.shegami.hr_saas.modules.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import com.shegami.hr_saas.modules.notifications.enums.EntityType;
import com.shegami.hr_saas.modules.notifications.enums.NotificationType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage extends BaseMessage {
    private String userId;
    private NotificationType notificationType;
    private String title;
    private String message;
    private EntityType entityType;
    private String entityId;
    private String actorId;
    private String actorName;
    private Map<String, Object> metadata;
}
