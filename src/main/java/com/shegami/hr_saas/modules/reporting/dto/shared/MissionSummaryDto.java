package com.shegami.hr_saas.modules.reporting.dto.shared;

import java.time.LocalDate;

public record MissionSummaryDto(
        String missionId, String title, String clientName,
        LocalDate startDate, LocalDate endDate, String status) {
}
