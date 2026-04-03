package com.shegami.hr_saas.modules.reporting.dto;

import com.shegami.hr_saas.modules.reporting.dto.shared.InvoiceSummaryDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.MissionDeadlineDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.MissionSummaryDto;

import java.util.List;

public record ConsultantDashboardDto(
        long activeMissionsCount,
        List<MissionSummaryDto> activeMissions,

        String currentTimesheetStatus,
        String currentTimesheetId,
        long pendingTimesheetsCount,
        double totalDaysWorkedThisMonth,

        List<InvoiceSummaryDto> recentInvoices,

        long unreadNotificationsCount,

        List<MissionDeadlineDto> upcomingDeadlines) {
}
