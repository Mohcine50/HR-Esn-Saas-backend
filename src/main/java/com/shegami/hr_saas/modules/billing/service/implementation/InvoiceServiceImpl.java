package com.shegami.hr_saas.modules.billing.service.implementation;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.config.domain.rabbitMq.RabbitMQConfig;
import com.shegami.hr_saas.modules.billing.entity.Invoice;
import com.shegami.hr_saas.modules.billing.entity.InvoiceLine;
import com.shegami.hr_saas.modules.billing.exception.InvoiceNotFoundException;
import com.shegami.hr_saas.modules.billing.enums.InvoiceStatus;
import com.shegami.hr_saas.modules.billing.repository.InvoiceRepository;
import com.shegami.hr_saas.modules.billing.service.InvoiceService;
import com.shegami.hr_saas.modules.billing.service.PdfGeneratorService;
import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.modules.timesheet.dto.TimesheetApprovedEvent;
import com.shegami.hr_saas.modules.timesheet.entity.Timesheet;
import com.shegami.hr_saas.modules.timesheet.entity.TimesheetEntry;
import com.shegami.hr_saas.modules.timesheet.exceptions.TimesheetNotFoundException;
import com.shegami.hr_saas.modules.timesheet.repository.TimesheetRepository;
import com.shegami.hr_saas.modules.notifications.dto.NotificationMessage;
import com.shegami.hr_saas.modules.notifications.enums.EntityType;
import com.shegami.hr_saas.modules.notifications.enums.NotificationType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;

import com.shegami.hr_saas.modules.upload.service.UploadService;
import com.shegami.hr_saas.modules.upload.mapper.FileType;
import com.shegami.hr_saas.modules.upload.entity.UploadFile;

import com.shegami.hr_saas.modules.billing.dto.InvoiceDto;
import com.shegami.hr_saas.modules.billing.dto.PaymentRequest;
import com.shegami.hr_saas.modules.billing.entity.Payment;
import com.shegami.hr_saas.modules.billing.mapper.BillingMapper;
import com.shegami.hr_saas.modules.billing.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

        private final InvoiceRepository invoiceRepository;
        private final PaymentRepository paymentRepository;
        private final TimesheetRepository timesheetRepository;
        private final PdfGeneratorService pdfGeneratorService;
        private final UploadService uploadService;
        private final BillingMapper billingMapper;
        private final RabbitTemplate rabbitTemplate;

        @RabbitListener(queues = RabbitMQConfig.BILLING_QUEUE)
        @Transactional
        @Override
        public void handleTimesheetApproved(TimesheetApprovedEvent event) {
                log.info("[Billing] Received TimesheetApprovedEvent for Timesheet ID: {}", event.getTimesheetId());

                Timesheet timesheet = timesheetRepository
                                .findByIdAndTenant(event.getTimesheetId(), event.getTenantId())
                                .orElseThrow(() -> new TimesheetNotFoundException(
                                                "Timesheet not found: " + event.getTimesheetId()));

                String invoiceRef = "INV-" + timesheet.getYear() + "-" + String.format("%02d", timesheet.getMonth())
                                + "-"
                                + event.getTimesheetId().substring(0, 5);

                Invoice invoice = new Invoice();
                invoice.setTenant(timesheet.getTenant());
                invoice.setInvoiceNumber(invoiceRef);

                Client client = timesheet.getMission().getClient();
                invoice.setClient(client);
                if (client != null) {
                        invoice.setClientNameAtBilling(client.getFullName());
                        invoice.setClientAddressAtBilling(
                                        client.getAddress() != null ? client.getAddress() : "Address not provided");
                        invoice.setVatNumberAtBilling(client.getVatNumber() != null ? client.getVatNumber() : "N/A");
                } else {
                        invoice.setClientNameAtBilling("Unknown Client");
                }

                invoice.setIssueDate(LocalDate.now());
                YearMonth ym = YearMonth.of(timesheet.getYear(), timesheet.getMonth());
                invoice.setDueDate(ym.atEndOfMonth().plusDays(30));
                invoice.setStatus(InvoiceStatus.DRAFT);
                invoice.setTimesheet(timesheet);

                BigDecimal subTotal = BigDecimal.ZERO;
                BigDecimal defaultTjm = BigDecimal.valueOf(500.00);

                for (TimesheetEntry entry : timesheet.getEntries()) {
                        if (entry.getQuantity() > 0) {
                                InvoiceLine line = new InvoiceLine();
                                line.setTenant(timesheet.getTenant());
                                line.setInvoice(invoice);
                                line.setDescription("Consulting (" + entry.getDate() + ") - "
                                                + timesheet.getMission().getTitle());
                                line.setQuantity(BigDecimal.valueOf(entry.getQuantity()));
                                line.setUnitPrice(defaultTjm);

                                BigDecimal lineTotal = line.getQuantity().multiply(line.getUnitPrice());
                                line.setTotalLineAmount(lineTotal);

                                invoice.getInvoiceLines().add(line);
                                subTotal = subTotal.add(lineTotal);
                        }
                }

                invoice.setSubTotal(subTotal);
                BigDecimal vatRate = BigDecimal.valueOf(0.20);
                BigDecimal vatAmount = subTotal.multiply(vatRate).setScale(2, RoundingMode.HALF_UP);
                invoice.setVatAmount(vatAmount);
                invoice.setTotalAmount(subTotal.add(vatAmount));

                Invoice savedInvoice = invoiceRepository.save(invoice);

                try {
                        byte[] pdfBytes = pdfGeneratorService.generateInvoicePdf(savedInvoice);
                        UploadFile uploadFile = uploadService.uploadInternalFile(
                                        pdfBytes,
                                        savedInvoice.getInvoiceNumber() + ".pdf",
                                        FileType.INVOICE,
                                        "application/pdf",
                                        savedInvoice.getTenant(),
                                        savedInvoice.getCreatedBy());

                        savedInvoice.setInvoiceFile(uploadFile);
                        savedInvoice.setPdfS3Key(uploadFile.getS3Key());
                        invoiceRepository.save(savedInvoice);

                        if (timesheet.getMission().getAccountManager() != null
                                        && timesheet.getMission().getAccountManager().getUser() != null) {
                                NotificationMessage msg = NotificationMessage.builder()
                                                .userId(timesheet.getMission().getAccountManager().getUser()
                                                                .getUserId())
                                                .notificationType(NotificationType.INVOICE_GENERATED)
                                                .title(NotificationType.INVOICE_GENERATED.getDefaultTitle())
                                                .message("An invoice for the mission '"
                                                                + timesheet.getMission().getTitle()
                                                                + "' was successfully generated.")
                                                .entityType(EntityType.INVOICE)
                                                .entityId(savedInvoice.getInvoiceId())
                                                .actorId(timesheet.getValidatedBy().getUser().getUserId())
                                                .actorName(timesheet.getValidatedBy().getUser().getFullName())
                                                .build();
                                rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                                "notification.invoice.generated",
                                                msg);
                        }
                } catch (Exception e) {
                        log.error("[Billing] Failed to generate/upload PDF for Invoice ID: {}",
                                        savedInvoice.getInvoiceId(), e);
                }
        }

        @Override
        public Page<InvoiceDto> getAllInvoices(Pageable pageable) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("[Billing] Fetching paginated invoices for tenant: {}", tenantId);
                return invoiceRepository.findAllByTenantTenantId(tenantId, pageable)
                                .map(inv -> {
                                        List<Payment> payments = paymentRepository
                                                        .findByInvoiceInvoiceIdAndTenantTenantId(inv.getInvoiceId(),
                                                                        tenantId);
                                        return billingMapper.toDto(inv, payments);
                                });
        }

        @Override
        public InvoiceDto getInvoiceById(String invoiceId) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("[Billing] Fetching invoice detail: {} for tenant: {}", invoiceId, tenantId);
                Invoice invoice = invoiceRepository.findByInvoiceIdAndTenantTenantId(invoiceId, tenantId)
                                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found: " + invoiceId));
                List<Payment> payments = paymentRepository.findByInvoiceInvoiceIdAndTenantTenantId(invoiceId, tenantId);
                return billingMapper.toDto(invoice, payments);
        }

        @Override
        @Transactional
        public void recordPayment(String invoiceId, PaymentRequest request) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("[Billing] Recording payment of {} for invoice: {} (Tenant: {})",
                                request.getAmount(), invoiceId, tenantId);

                Invoice invoice = invoiceRepository.findByInvoiceIdAndTenantTenantId(invoiceId, tenantId)
                                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found: " + invoiceId));

                Payment payment = new Payment();
                payment.setInvoice(invoice);
                payment.setAmount(request.getAmount());
                payment.setPaymentDate(request.getPaymentDate());
                payment.setTransactionReference(request.getTransactionReference());
                payment.setMethod(request.getMethod());
                payment.setTenant(invoice.getTenant());

                paymentRepository.save(payment);

                // Update invoice status if fully paid
                List<Payment> payments = paymentRepository.findByInvoiceInvoiceIdAndTenantTenantId(invoiceId, tenantId);
                BigDecimal totalPaid = payments.stream()
                                .map(Payment::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                log.debug("[Billing] Total paid for invoice {}: {} / {}", invoiceId, totalPaid,
                                invoice.getTotalAmount());

                if (totalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
                        log.info("[Billing] Invoice {} is now fully paid. Updating status to PAID.", invoiceId);
                        invoice.setStatus(InvoiceStatus.PAID);
                        invoiceRepository.save(invoice);
                }
        }

        @Override
        @Transactional
        public void updateStatus(String invoiceId, InvoiceStatus status) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("[Billing] Updating status of invoice {} to {} (Tenant: {})", invoiceId, status, tenantId);
                Invoice invoice = invoiceRepository.findByInvoiceIdAndTenantTenantId(invoiceId, tenantId)
                                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found: " + invoiceId));
                invoice.setStatus(status);
                invoiceRepository.save(invoice);
        }

        @Override
        public String getDownloadUrl(String invoiceId) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("[Billing] Generating download URL for invoice: {} (Tenant: {})", invoiceId, tenantId);
                Invoice invoice = invoiceRepository.findByInvoiceIdAndTenantTenantId(invoiceId, tenantId)
                                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found: " + invoiceId));

                if (invoice.getInvoiceFile() != null) {
                        String url = uploadService.resolveUrl(invoice.getInvoiceFile());
                        log.debug("[Billing] Download URL generated: {}", url);
                        return url;
                }
                log.warn("[Billing] No file found for invoice: {}", invoiceId);
                return null;
        }
}
