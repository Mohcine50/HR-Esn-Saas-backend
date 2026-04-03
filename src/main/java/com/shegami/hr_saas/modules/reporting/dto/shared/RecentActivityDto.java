package com.shegami.hr_saas.modules.reporting.dto.shared;

import java.time.LocalDateTime;

public record RecentActivityDto(
        String type, String title, String message,
        LocalDateTime createdAt, String actorName) {
}
