package com.shegami.hr_saas.modules.reporting.dto.shared;

import java.time.LocalDate;

public record MissionDeadlineDto(
        String missionId, String title, String clientName,
        LocalDate endDate, long daysRemaining) {
}
