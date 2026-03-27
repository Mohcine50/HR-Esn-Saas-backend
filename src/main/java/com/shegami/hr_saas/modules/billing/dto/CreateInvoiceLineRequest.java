package com.shegami.hr_saas.modules.billing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateInvoiceLineRequest {
    @NotBlank
    private String description;

    @NotNull
    @Min(0)
    private BigDecimal quantity;

    @NotNull
    @Min(0)
    private BigDecimal unitPrice;
}
