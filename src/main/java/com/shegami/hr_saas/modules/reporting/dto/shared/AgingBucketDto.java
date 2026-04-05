package com.shegami.hr_saas.modules.reporting.dto.shared;

import java.math.BigDecimal;

public record AgingBucketDto(
        String bucketLabel,
        long invoiceCount,
        BigDecimal totalAmount) {
}
