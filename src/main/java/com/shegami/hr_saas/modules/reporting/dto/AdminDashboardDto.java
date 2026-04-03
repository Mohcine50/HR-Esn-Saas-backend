package com.shegami.hr_saas.modules.reporting.dto;

import com.shegami.hr_saas.modules.hr.dto.EmployeesCountByContract;
import com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.RecentActivityDto;

import java.util.List;

public record AdminDashboardDto(
        long totalEmployees,
        List<EmployeesCountByContract> employeesByContractType,
        List<CountByStatusDto> employeesByStatus,
        long pendingInvitationsCount,

        long totalConsultants,
        List<CountByStatusDto> consultantsByStatus,
        double utilizationRate,

        long activeProjectsCount,
        long totalProjectsCount,
        long activeMissionsCount,
        List<CountByStatusDto> missionsByStatus,

        long totalClients,

        long pendingTimesheetApprovals,

        List<RecentActivityDto> recentActivities) {
}
