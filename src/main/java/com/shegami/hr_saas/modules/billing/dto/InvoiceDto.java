package com.shegami.hr_saas.modules.billing.dto;

import com.shegami.hr_saas.modules.billing.enums.InvoiceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class InvoiceDto {
    private String invoiceId;
    private String invoiceNumber;
    private String clientId;
    private String clientNameAtBilling;
    private String clientAddressAtBilling;
    private String vatNumberAtBilling;
    private BigDecimal subTotal;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private String pdfS3Key;
    private String pdfUrl; // Temporary URL for frontend to download
    private List<InvoiceLineDto> lines;
    private List<PaymentDto> payments;
}
