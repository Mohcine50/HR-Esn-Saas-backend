package com.shegami.hr_saas.modules.reporting.dto.shared;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentSummaryDto(
        String paymentId, BigDecimal amount,
        LocalDate paymentDate, String method, String invoiceNumber) {
}
