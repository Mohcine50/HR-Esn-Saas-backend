package com.shegami.hr_saas.modules.billing.dto;

import com.shegami.hr_saas.modules.billing.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Transaction reference is required")
    private String transactionReference;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;
}
