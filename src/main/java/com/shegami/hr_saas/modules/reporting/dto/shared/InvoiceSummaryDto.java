package com.shegami.hr_saas.modules.reporting.dto.shared;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceSummaryDto(
        String invoiceId, String invoiceNumber,
        BigDecimal totalAmount, String status, LocalDate issueDate) {
}
