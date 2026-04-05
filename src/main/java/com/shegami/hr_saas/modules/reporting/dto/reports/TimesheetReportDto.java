package com.shegami.hr_saas.modules.reporting.dto.reports;

import com.shegami.hr_saas.modules.reporting.dto.shared.ConsultantHoursDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.MonthlyCountDto;

import java.util.List;

public record TimesheetReportDto(
        // KPIs
        long totalTimesheets,
        double submissionRate,
        double approvalRate,
        double rejectionRate,
        double avgDaysWorkedPerMonth,

        // Charts
        List<CountByStatusDto> timesheetsByStatus,
        List<MonthlyCountDto> monthlySubmissions,
        List<ConsultantHoursDto> topConsultantsByHours) {
}
