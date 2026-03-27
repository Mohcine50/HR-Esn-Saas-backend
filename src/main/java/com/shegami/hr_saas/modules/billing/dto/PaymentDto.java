package com.shegami.hr_saas.modules.billing.dto;

import com.shegami.hr_saas.modules.billing.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PaymentDto {
    private String paymentId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String transactionReference;
    private PaymentMethod method;
}
