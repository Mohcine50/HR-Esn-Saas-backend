package com.shegami.hr_saas.modules.billing.dto;

import com.shegami.hr_saas.modules.billing.enums.InvoiceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateInvoiceStatusRequest {
    @NotNull(message = "Invoice status is required")
    private InvoiceStatus status;
}
