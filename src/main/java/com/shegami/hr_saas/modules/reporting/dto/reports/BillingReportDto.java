package com.shegami.hr_saas.modules.reporting.dto.reports;

import com.shegami.hr_saas.modules.reporting.dto.shared.AgingBucketDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.MonthlyRevenueDto;

import java.math.BigDecimal;
import java.util.List;

public record BillingReportDto(
        // KPIs
        BigDecimal totalInvoiced,
        BigDecimal totalPaid,
        BigDecimal totalOutstanding,
        BigDecimal totalOverdue,
        double collectionRate,
        double averageDaysSalesOutstanding,

        // Charts
        List<CountByStatusDto> invoicesByStatus,
        List<MonthlyRevenueDto> monthlyInvoiced,
        List<AgingBucketDto> invoiceAgingBuckets,
        List<CountByStatusDto> paymentsByMethod) {
}
