package com.shegami.hr_saas.modules.reporting.dto;

import com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.MonthlyRevenueDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.PaymentSummaryDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.RevenueByClientDto;

import java.math.BigDecimal;
import java.util.List;

public record FinancialDashboardDto(
        BigDecimal totalRevenueThisMonth,
        BigDecimal totalRevenueThisYear,
        BigDecimal outstandingAmount,
        BigDecimal averageInvoiceAmount,

        long totalInvoices,
        List<CountByStatusDto> invoicesByStatus,
        long overdueInvoicesCount,
        BigDecimal overdueAmount,

        List<PaymentSummaryDto> recentPayments,

        List<RevenueByClientDto> topClientsByRevenue,

        long approvedTimesheetsReadyForInvoicing,

        List<MonthlyRevenueDto> monthlyRevenueTrend) {
}
