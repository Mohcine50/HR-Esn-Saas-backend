package com.shegami.hr_saas.modules.reporting.dto.reports;

import com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.MonthlyUtilizationDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.SkillCountDto;

import java.math.BigDecimal;
import java.util.List;

public record WorkforceReportDto(
        // KPIs
        double utilizationRate,
        long totalConsultants,
        long benchedConsultantsCount,
        BigDecimal averageInternalDailyCost,

        // Charts
        List<MonthlyUtilizationDto> monthlyUtilization,
        List<CountByStatusDto> consultantsByStatus,
        List<CountByStatusDto> consultantsBySeniority,
        List<CountByStatusDto> consultantsByType,
        List<SkillCountDto> topSkills) {
}
