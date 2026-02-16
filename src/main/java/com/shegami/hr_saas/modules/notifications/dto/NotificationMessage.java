package com.shegami.hr_saas.modules.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage extends BaseMessage {
    private String userId;
    private String notificationType; // MISSION_ASSIGNED, MENTION, COMMENT, etc.
    private String title;
    private String message;
    private String entityType; // MISSION, TIMESHEET, etc.
    private String entityId;
    private String actorId;
    private String actorName;
    private Map<String, Object> metadata;
}

