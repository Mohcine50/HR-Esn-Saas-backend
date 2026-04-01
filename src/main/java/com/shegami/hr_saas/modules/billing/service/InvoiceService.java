package com.shegami.hr_saas.modules.billing.service;

import com.shegami.hr_saas.config.domain.rabbitMq.RabbitMQConfig;
import com.shegami.hr_saas.modules.billing.dto.InvoiceDto;
import com.shegami.hr_saas.modules.billing.dto.PaymentRequest;
import com.shegami.hr_saas.modules.billing.enums.InvoiceStatus;
import com.shegami.hr_saas.modules.timesheet.dto.TimesheetApprovedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.transaction.annotation.Transactional;

import com.shegami.hr_saas.modules.billing.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvoiceService {
    @RabbitListener(queues = RabbitMQConfig.BILLING_QUEUE)
    @Transactional
    void handleTimesheetApproved(TimesheetApprovedEvent event);

    InvoiceDto createInvoice(CreateInvoiceRequest request);

    Page<InvoiceDto> getAllInvoices(Pageable pageable);

    InvoiceDto getInvoiceById(String invoiceId);

    void recordPayment(String invoiceId, PaymentRequest request);

    InvoiceDto updateInvoice(String invoiceId, CreateInvoiceRequest request);

    void deleteInvoice(String invoiceId);

    void updateStatus(String invoiceId, InvoiceStatus status);

    String getDownloadUrl(String invoiceId);

    Page<InvoiceDto> getPaidInvoicesByConsultant(Pageable pageable);

    Page<InvoiceDto> getInvoicesByConsultant(InvoiceStatus status, Pageable pageable);
}
