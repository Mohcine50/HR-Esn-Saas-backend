package com.shegami.hr_saas.modules.reporting.dto.reports;

import com.shegami.hr_saas.modules.reporting.dto.shared.MonthlyRevenueDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.RevenueByClientDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.RevenueByProjectDto;

import java.math.BigDecimal;
import java.util.List;

public record RevenueReportDto(
        // KPIs
        BigDecimal totalRevenue,
        BigDecimal totalRevenueLastPeriod,
        double revenueGrowthPercentage,
        BigDecimal averageDailyRate,

        // Charts
        List<MonthlyRevenueDto> monthlyRevenue,
        List<RevenueByClientDto> revenueByClient,
        List<RevenueByProjectDto> revenueByProject) {
}
