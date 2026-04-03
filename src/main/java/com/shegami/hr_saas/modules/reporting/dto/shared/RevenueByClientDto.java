package com.shegami.hr_saas.modules.reporting.dto.shared;

import java.math.BigDecimal;

public record RevenueByClientDto(String clientName, BigDecimal totalPaid) {
}
