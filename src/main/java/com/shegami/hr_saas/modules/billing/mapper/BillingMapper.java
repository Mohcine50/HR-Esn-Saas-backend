package com.shegami.hr_saas.modules.billing.mapper;

import com.shegami.hr_saas.modules.billing.dto.InvoiceDto;
import com.shegami.hr_saas.modules.billing.dto.InvoiceLineDto;
import com.shegami.hr_saas.modules.billing.dto.PaymentDto;
import com.shegami.hr_saas.modules.billing.entity.Invoice;
import com.shegami.hr_saas.modules.billing.entity.InvoiceLine;
import com.shegami.hr_saas.modules.billing.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface BillingMapper {

    @Mapping(source = "invoice.client.clientId", target = "clientId")
    @Mapping(source = "invoice.invoiceLines", target = "lines")
    @Mapping(source = "payments", target = "payments")
    @Mapping(target = "pdfUrl", ignore = true)
    InvoiceDto toDto(Invoice invoice, List<Payment> payments);

    InvoiceLineDto toDto(InvoiceLine line);

    PaymentDto toDto(Payment payment);

    List<PaymentDto> toPaymentDtoList(List<Payment> payments);

    List<InvoiceLineDto> toInvoiceLineDtoList(List<InvoiceLine> lines);
}
