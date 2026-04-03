package com.shegami.hr_saas.modules.reporting.dto.shared;

import java.math.BigDecimal;

public record MonthlyRevenueDto(int month, int year, BigDecimal totalRevenue) {
}
