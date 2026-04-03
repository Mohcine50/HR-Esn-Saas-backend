package com.shegami.hr_saas.modules.reporting.dto;

import com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.MissionDeadlineDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.MissionSummaryDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.RecentActivityDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.TimesheetApprovalDto;

import java.util.List;

public record ManagerDashboardDto(
        long managedMissionsCount,
        List<CountByStatusDto> managedMissionsByStatus,
        List<MissionSummaryDto> activeManagedMissions,

        long pendingTimesheetApprovals,
        List<TimesheetApprovalDto> pendingTimesheets,

        long teamConsultantsCount,
        List<CountByStatusDto> teamConsultantsByStatus,

        double totalApprovedDaysThisMonth,
        long rejectedTimesheetsThisMonth,

        List<MissionDeadlineDto> upcomingMissionEnds,

        List<RecentActivityDto> recentMissionActivities) {
}
