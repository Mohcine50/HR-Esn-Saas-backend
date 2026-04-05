package com.shegami.hr_saas.modules.reporting.dto.reports;

import com.shegami.hr_saas.modules.reporting.dto.shared.ClientMissionCountDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.MonthlyCountDto;

import java.util.List;

public record MissionReportDto(
        // KPIs
        long totalMissions,
        long activeMissions,
        long completedMissions,
        double averageMissionDurationDays,
        double missionCompletionRate,

        // Charts
        List<CountByStatusDto> missionsByStatus,
        List<CountByStatusDto> missionsByPriority,
        List<MonthlyCountDto> missionsStartedByMonth,
        List<ClientMissionCountDto> clientsWithMostMissions) {
}
