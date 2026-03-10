package com.shegami.hr_saas.modules.mission.dto;

import com.shegami.hr_saas.modules.mission.enums.ActivityType;

import java.time.LocalDateTime;

public record MissionActivityResponse(
        String       activityId,
        ActivityType type,
        String       field,
        String       description,
        String       fromValue,
        String       toValue,
        String       actorId,
        String       actorName,
        LocalDateTime createdAt
) {}
