package com.shegami.hr_saas.modules.billing.service.implementation;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.config.domain.rabbitMq.RabbitMQConfig;
import com.shegami.hr_saas.modules.billing.entity.Invoice;
import com.shegami.hr_saas.modules.billing.entity.InvoiceLine;
import com.shegami.hr_saas.modules.billing.exception.IllegalInvoiceStateException;
import com.shegami.hr_saas.modules.billing.exception.InvoiceFileNotFoundException;
import com.shegami.hr_saas.modules.billing.exception.InvoiceNotFoundException;
import com.shegami.hr_saas.modules.billing.enums.InvoiceStatus;
import com.shegami.hr_saas.modules.billing.repository.InvoiceRepository;
import com.shegami.hr_saas.modules.billing.service.InvoiceService;
import com.shegami.hr_saas.modules.billing.service.PdfGeneratorService;
import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.modules.mission.exceptions.ClientNotFoundException;
import com.shegami.hr_saas.modules.timesheet.dto.TimesheetApprovedEvent;
import com.shegami.hr_saas.modules.timesheet.entity.Timesheet;
import com.shegami.hr_saas.modules.timesheet.exceptions.TimesheetNotFoundException;
import com.shegami.hr_saas.modules.timesheet.repository.TimesheetRepository;
import com.shegami.hr_saas.modules.notifications.dto.NotificationMessage;
import com.shegami.hr_saas.modules.notifications.enums.EntityType;
import com.shegami.hr_saas.modules.notifications.enums.NotificationType;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import org.springframework.util.StringUtils;

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
import com.shegami.hr_saas.modules.upload.dto.FileUploadMessage;

import com.shegami.hr_saas.modules.billing.dto.InvoiceDto;
import com.shegami.hr_saas.modules.billing.dto.PaymentRequest;
import com.shegami.hr_saas.modules.billing.entity.Payment;
import com.shegami.hr_saas.modules.billing.mapper.BillingMapper;
import com.shegami.hr_saas.modules.billing.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.mission.repository.ClientRepository;
import com.shegami.hr_saas.modules.billing.dto.CreateInvoiceRequest;
import com.shegami.hr_saas.modules.billing.dto.CreateInvoiceLineRequest;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import java.util.Collections;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
        private final ClientRepository clientRepository;
        private final TenantService tenantService;

        // -------------------------------------------------------------------------
        // Public API — Manual Invoice Creation
        // -------------------------------------------------------------------------

        @Override
        @Transactional
        public InvoiceDto createInvoice(CreateInvoiceRequest request) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("[Invoice] Creating manual invoice | tenantId={} clientId={}", tenantId,
                                request.getClientId());

                Tenant tenant = tenantService.getTenant(tenantId);
                Client client = resolveClient(request.getClientId(), tenantId);

                Invoice invoice = buildManualInvoice(request, tenant, client);
                applyInvoiceLines(invoice, tenant, request.getLines());
                calculateInvoiceTotals(invoice);

                Invoice savedInvoice = invoiceRepository.save(invoice);
                log.info("[Invoice] Invoice persisted | invoiceId={} invoiceNumber={}",
                                savedInvoice.getInvoiceId(), savedInvoice.getInvoiceNumber());

                // PDF generation runs after commit — keeps the transaction short and ensures
                // the invoice row is visible to any other process before we start slow I/O.
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                                generateAndDispatchPdf(savedInvoice, tenant, null);
                        }
                });

                return billingMapper.toDto(savedInvoice, Collections.emptyList());
        }

        @Override
        @Transactional
        public InvoiceDto updateInvoice(String invoiceId, CreateInvoiceRequest request) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("[Invoice] Updating invoice | invoiceId={} tenantId={}", invoiceId, tenantId);

                Invoice invoice = resolveInvoice(invoiceId, tenantId);

                if (invoice.getStatus() != InvoiceStatus.DRAFT) {
                        throw new IllegalInvoiceStateException(
                                        "Only DRAFT invoices can be updated. Current status: " + invoice.getStatus());
                }

                Client client = resolveClient(request.getClientId(), tenantId);
                populateClientSnapshot(invoice, client);
                invoice.setIssueDate(request.getIssueDate());
                invoice.setDueDate(request.getDueDate());

                invoice.getInvoiceLines().clear();
                applyInvoiceLines(invoice, invoice.getTenant(), request.getLines());
                calculateInvoiceTotals(invoice);

                Tenant tenant = invoice.getTenant(); // capture before transaction closes
                Invoice savedInvoice = invoiceRepository.save(invoice);
                log.info("[Invoice] Invoice updated | invoiceId={}", savedInvoice.getInvoiceId());

                // PDF regeneration runs after commit for the same reason as createInvoice —
                // transaction stays short, invoice is committed before any I/O begins.
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                                generateAndDispatchPdf(savedInvoice, tenant, null);
                        }
                });

                return billingMapper.toDto(savedInvoice, invoice.getPayments());
        }

        @Override
        @Transactional(readOnly = true)
        public Page<InvoiceDto> getAllInvoices(Pageable pageable) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.debug("[Invoice] Fetching paginated invoices | tenantId={} page={} size={}",
                                tenantId, pageable.getPageNumber(), pageable.getPageSize());

                return invoiceRepository.findAllByTenantTenantId(tenantId, pageable)
                                .map(inv -> {
                                        List<Payment> payments = paymentRepository
                                                        .findByInvoiceInvoiceIdAndTenantTenantId(inv.getInvoiceId(),
                                                                        tenantId);
                                        return billingMapper.toDto(inv, payments);
                                });
        }

        @Override
        @Transactional(readOnly = true)
        public InvoiceDto getInvoiceById(String invoiceId) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.debug("[Invoice] Fetching invoice | invoiceId={} tenantId={}", invoiceId, tenantId);

                Invoice invoice = resolveInvoice(invoiceId, tenantId);
                List<Payment> payments = paymentRepository
                                .findByInvoiceInvoiceIdAndTenantTenantId(invoiceId, tenantId);

                return billingMapper.toDto(invoice, payments);
        }

        @Override
        @Transactional
        public void recordPayment(String invoiceId, PaymentRequest request) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("[Invoice] Recording payment | invoiceId={} amount={} tenantId={}",
                                invoiceId, request.getAmount(), tenantId);

                Invoice invoice = resolveInvoice(invoiceId, tenantId);

                Payment payment = new Payment();
                payment.setInvoice(invoice);
                payment.setAmount(request.getAmount());
                payment.setPaymentDate(request.getPaymentDate());
                payment.setTransactionReference(request.getTransactionReference());
                payment.setMethod(request.getMethod());
                payment.setTenant(invoice.getTenant());
                paymentRepository.save(payment);

                List<Payment> allPayments = paymentRepository
                                .findByInvoiceInvoiceIdAndTenantTenantId(invoiceId, tenantId);

                BigDecimal totalPaid = allPayments.stream()
                                .map(Payment::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                log.debug("[Invoice] Payment totals | invoiceId={} paid={} total={}",
                                invoiceId, totalPaid, invoice.getTotalAmount());

                if (totalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
                        log.info("[Invoice] Invoice fully paid, updating status | invoiceId={}", invoiceId);
                        invoice.setStatus(InvoiceStatus.PAID);
                        invoiceRepository.save(invoice);
                }
        }

        @Override
        @Transactional
        public void updateStatus(String invoiceId, InvoiceStatus status) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("[Invoice] Updating status | invoiceId={} newStatus={} tenantId={}", invoiceId, status,
                                tenantId);

                Invoice invoice = resolveInvoice(invoiceId, tenantId);
                invoice.setStatus(status);
                invoiceRepository.save(invoice);
        }

        @Override
        @Transactional
        public void deleteInvoice(String invoiceId) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("[Invoice] Deleting invoice | invoiceId={} tenantId={}", invoiceId, tenantId);

                Invoice invoice = resolveInvoice(invoiceId, tenantId);

                if (invoice.getStatus() == InvoiceStatus.PAID) {
                        throw new IllegalInvoiceStateException("Cannot delete a PAID invoice.");
                }

                invoiceRepository.delete(invoice);
                log.info("[Invoice] Invoice deleted | invoiceId={}", invoiceId);
        }

        @Override
        @Transactional(readOnly = true)
        public String getDownloadUrl(String invoiceId) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.debug("[Invoice] Generating download URL | invoiceId={} tenantId={}", invoiceId, tenantId);

                Invoice invoice = resolveInvoice(invoiceId, tenantId);

                if (invoice.getInvoiceFile() == null) {
                        log.warn("[Invoice] No PDF file attached to invoice | invoiceId={}", invoiceId);
                        throw new InvoiceFileNotFoundException("No PDF available for invoice: " + invoiceId);
                }

                String url = uploadService.resolveUrl(invoice.getInvoiceFile());
                log.debug("[Invoice] Download URL resolved | invoiceId={}", invoiceId);
                return url;
        }

        // -------------------------------------------------------------------------
        // RabbitMQ — Timesheet Approved Event Handler
        // -------------------------------------------------------------------------

        @RabbitListener(queues = RabbitMQConfig.BILLING_QUEUE)
        @Override
        public void handleTimesheetApproved(TimesheetApprovedEvent event) {
                log.info("Received TimesheetApprovedEvent | timesheetId={} tenantId={}",
                                event.getTimesheetId(), event.getTenantId());

                // Idempotency guard — safe against RabbitMQ message redelivery
                if (invoiceRepository.existsByTimesheetTimesheetId(event.getTimesheetId())) {
                        log.warn("Duplicate event detected, invoice already exists | timesheetId={}",
                                        event.getTimesheetId());
                        return;
                }

                Timesheet timesheet = timesheetRepository
                                .findByIdAndTenant(event.getTimesheetId(), event.getTenantId())
                                .orElseThrow(() -> new TimesheetNotFoundException(
                                                "Timesheet not found: " + event.getTimesheetId()));

                log.info("Timesheet resolved | timesheetId={} status={}",
                                timesheet.getTimesheetId(), timesheet.getStatus());

                // Step 1: Persist invoice — short, focused transaction
                Invoice savedInvoice = createAndSaveInvoice(timesheet);
                log.info("Invoice persisted | invoiceId={}", savedInvoice.getInvoiceId());

                // Step 2: Generate PDF — I/O outside any transaction
                byte[] pdfBytes;
                try {
                        pdfBytes = pdfGeneratorService.generateInvoicePdf(savedInvoice);
                        log.info("PDF generated | invoiceId={} sizeBytes={}", savedInvoice.getInvoiceId(),
                                        pdfBytes.length);
                } catch (Exception e) {
                        log.error("PDF generation failed | invoiceId={}", savedInvoice.getInvoiceId(), e);
                        markAsPdfPending(savedInvoice.getInvoiceId());
                        // Invoice is persisted safely — a retry scheduler can regenerate the PDF later
                        // Do NOT notify the user: invoice is incomplete at this point
                        return;
                }

                // Step 3: Attach PDF metadata — another short, focused transaction
                Invoice invoiceWithPdf;
                try {
                        invoiceWithPdf = attachPdfToInvoice(savedInvoice, pdfBytes, timesheet.getTenant());
                        log.info("PDF metadata attached | invoiceId={}", invoiceWithPdf.getInvoiceId());
                } catch (Exception e) {
                        log.error("Failed to attach PDF to invoice | invoiceId={}",
                                        savedInvoice.getInvoiceId(), e);
                        markAsPdfPending(savedInvoice.getInvoiceId());
                        return;
                }

                // Step 4: Dispatch async S3 upload — fire and forget
                dispatchUploadTask(invoiceWithPdf, pdfBytes);

                // Step 5: Notify only after full success
                sendInvoiceGeneratedNotification(invoiceWithPdf, timesheet);
        }

        @Transactional
        public Invoice createAndSaveInvoice(Timesheet timesheet) {
                Invoice invoice = buildInvoiceFromTimesheet(timesheet);
                calculateInvoiceTotals(invoice);
                return invoiceRepository.save(invoice);
        }

        @Transactional
        public Invoice attachPdfToInvoice(Invoice invoice, byte[] pdfBytes, Tenant tenant) {
                UploadFile uploadFile = uploadService.uploadInternalFile(
                                pdfBytes,
                                invoice.getInvoiceNumber() + ".pdf",
                                FileType.INVOICE,
                                "application/pdf",
                                tenant,
                                invoice.getCreatedBy());

                invoice.setInvoiceFile(uploadFile);
                invoice.setPdfS3Key(uploadFile.getS3Key());
                return invoiceRepository.save(invoice);
        }

        @Transactional
        public void markAsPdfPending(String invoiceId) {
                invoiceRepository.findById(invoiceId).ifPresentOrElse(
                                invoice -> {
                                        invoice.setStatus(InvoiceStatus.PDF_PENDING);
                                        invoiceRepository.save(invoice);
                                        log.warn("[Invoice] Marked as PDF_PENDING | invoiceId={}", invoiceId);
                                },
                                () -> log.error("[Invoice] Cannot mark PDF_PENDING — invoice not found | invoiceId={}",
                                                invoiceId));
        }

        private Invoice buildManualInvoice(CreateInvoiceRequest request, Tenant tenant, Client client) {
                String invoiceNumber = String.format("INV-M-%s-%s",
                                LocalDate.now().toString().replace("-", ""),
                                UUID.randomUUID().toString().substring(0, 5).toUpperCase());

                Invoice invoice = new Invoice();
                invoice.setTenant(tenant);
                invoice.setInvoiceNumber(invoiceNumber);
                invoice.setClient(client);
                invoice.setIssueDate(request.getIssueDate());
                invoice.setDueDate(request.getDueDate());
                invoice.setStatus(InvoiceStatus.DRAFT);
                populateClientSnapshot(invoice, client);
                return invoice;
        }

        private Invoice buildInvoiceFromTimesheet(Timesheet timesheet) {
                String invoiceNumber = String.format("INV-%d-%02d-%s",
                                timesheet.getYear(),
                                timesheet.getMonth(),
                                timesheet.getTimesheetId().substring(0, 5));

                Invoice invoice = new Invoice();
                invoice.setTenant(timesheet.getTenant());
                invoice.setInvoiceNumber(invoiceNumber);
                invoice.setTimesheet(timesheet);
                invoice.setIssueDate(LocalDate.now());
                invoice.setStatus(InvoiceStatus.DRAFT);

                YearMonth ym = YearMonth.of(timesheet.getYear(), timesheet.getMonth());
                invoice.setDueDate(ym.atEndOfMonth().plusDays(30));

                Client client = timesheet.getMission().getClient();
                invoice.setClient(client);
                populateClientSnapshot(invoice, client);
                populateTimesheetLines(invoice, timesheet);

                return invoice;
        }

        private void populateClientSnapshot(Invoice invoice, Client client) {
                if (client != null) {
                        invoice.setClientNameAtBilling(client.getFullName());
                        invoice.setClientAddressAtBilling(
                                        StringUtils.hasText(client.getAddress()) ? client.getAddress()
                                                        : "Address not provided");
                        invoice.setVatNumberAtBilling(
                                        StringUtils.hasText(client.getVatNumber()) ? client.getVatNumber() : "N/A");
                } else {
                        log.warn("[Invoice] No client found — using fallback snapshot values");
                        invoice.setClientNameAtBilling("Unknown Client");
                        invoice.setClientAddressAtBilling("N/A");
                        invoice.setVatNumberAtBilling("N/A");
                }
        }

        private void applyInvoiceLines(Invoice invoice, Tenant tenant, List<CreateInvoiceLineRequest> lineRequests) {
                lineRequests.forEach(lineReq -> {
                        InvoiceLine line = new InvoiceLine();
                        line.setTenant(tenant);
                        line.setInvoice(invoice);
                        line.setDescription(lineReq.getDescription());
                        line.setQuantity(lineReq.getQuantity());
                        line.setUnitPrice(lineReq.getUnitPrice());
                        line.setTotalLineAmount(line.getQuantity().multiply(line.getUnitPrice()));
                        invoice.getInvoiceLines().add(line);
                });
        }

        private void populateTimesheetLines(Invoice invoice, Timesheet timesheet) {
                // TODO: Daily rate should be sourced from Mission or Contract terms, not
                // hardcoded.
                // Hardcoding a rate is a business risk — billing the wrong amount silently.
                BigDecimal dailyRate = BigDecimal.valueOf(500.00); // Using hardcoded fallback until Mission/Contract
                                                                   // integration

                timesheet.getEntries().stream()
                                .filter(entry -> entry.getQuantity() > 0)
                                .forEach(entry -> {
                                        InvoiceLine line = new InvoiceLine();
                                        line.setTenant(timesheet.getTenant());
                                        line.setInvoice(invoice);
                                        line.setDescription(String.format("Consulting (%s) - %s",
                                                        entry.getDate(), timesheet.getMission().getTitle()));
                                        line.setQuantity(BigDecimal.valueOf(entry.getQuantity()));
                                        line.setUnitPrice(dailyRate);
                                        line.setTotalLineAmount(line.getQuantity().multiply(line.getUnitPrice()));
                                        invoice.getInvoiceLines().add(line);
                                });
        }

        private void calculateInvoiceTotals(Invoice invoice) {
                BigDecimal subTotal = invoice.getInvoiceLines().stream()
                                .map(InvoiceLine::getTotalLineAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // TODO: VAT rate should be country/client/tenant-specific, not a global
                // constant.
                BigDecimal vatRate = BigDecimal.valueOf(0.20);
                BigDecimal vatAmount = subTotal.multiply(vatRate).setScale(2, RoundingMode.HALF_UP);

                invoice.setSubTotal(subTotal);
                invoice.setVatAmount(vatAmount);
                invoice.setTotalAmount(subTotal.add(vatAmount));
        }

        // -------------------------------------------------------------------------
        // Internal — PDF & Notification Dispatch
        // -------------------------------------------------------------------------

        /**
         * Generates a PDF and dispatches the S3 upload task.
         * Called from transactional methods (createInvoice, updateInvoice) after the
         * transaction commits. PDF failure is logged but does not roll back the
         * invoice.
         */
        private void generateAndDispatchPdf(Invoice invoice, Tenant tenant, User createdBy) {
                try {
                        byte[] pdfBytes = pdfGeneratorService.generateInvoicePdf(invoice);
                        log.info("PDF generated | invoiceId={} sizeBytes={}", invoice.getInvoiceId(),
                                        pdfBytes.length);

                        UploadFile uploadFile = uploadService.uploadInternalFile(
                                        pdfBytes,
                                        invoice.getInvoiceNumber() + ".pdf",
                                        FileType.INVOICE,
                                        "application/pdf",
                                        tenant,
                                        createdBy);

                        invoice.setInvoiceFile(uploadFile);
                        invoice.setPdfS3Key(uploadFile.getS3Key());
                        invoiceRepository.save(invoice);

                        log.info("[Invoice] PDF metadata saved | invoiceId={} fileId={}",
                                        invoice.getInvoiceId(), uploadFile.getFileId());

                        rabbitTemplate.convertAndSend(RabbitMQConfig.UPLOAD_QUEUE,
                                        new FileUploadMessage(uploadFile.getFileId(), pdfBytes));

                } catch (Exception e) {
                        log.error("[Invoice] PDF generation/upload failed | invoiceId={} — invoice is saved, PDF pending retry",
                                        invoice.getInvoiceId(), e);
                        markAsPdfPending(invoice.getInvoiceId());
                }
        }

        private void dispatchUploadTask(Invoice invoice, byte[] pdfBytes) {
                if (invoice.getInvoiceFile() == null) {
                        log.error("[Billing] Cannot dispatch upload — no file reference on invoice | invoiceId={}",
                                        invoice.getInvoiceId());
                        return;
                }
                log.info("[Billing] Dispatching async S3 upload | invoiceId={} fileId={}",
                                invoice.getInvoiceId(), invoice.getInvoiceFile().getFileId());
                rabbitTemplate.convertAndSend(RabbitMQConfig.UPLOAD_QUEUE,
                                new FileUploadMessage(invoice.getInvoiceFile().getFileId(), pdfBytes));
        }

        private void sendInvoiceGeneratedNotification(Invoice invoice, Timesheet timesheet) {
                Employee validatedBy = timesheet.getValidatedBy();
                Employee accountManager = timesheet.getMission().getAccountManager();

                if (accountManager == null || accountManager.getUser() == null) {
                        log.warn("[Billing] Skipping notification — no account manager on mission | invoiceId={}",
                                        invoice.getInvoiceId());
                        return;
                }

                if (validatedBy == null || validatedBy.getUser() == null) {
                        log.warn("[Billing] Skipping notification — timesheet has no validator | invoiceId={}",
                                        invoice.getInvoiceId());
                        return;
                }

                NotificationMessage msg = NotificationMessage.builder()
                                .userId(timesheet.getCreatedBy().getUserId())
                                .notificationType(NotificationType.INVOICE_GENERATED)
                                .title(NotificationType.INVOICE_GENERATED.getDefaultTitle())
                                .message(String.format("An invoice for the mission '%s' was successfully generated.",
                                                timesheet.getMission().getTitle()))
                                .entityType(EntityType.INVOICE)
                                .entityId(invoice.getInvoiceId())
                                .actorId(validatedBy.getUser().getUserId())
                                .actorName(validatedBy.getUser().getFullName())
                                .build();

                log.info("[Billing] Publishing INVOICE_GENERATED notification | invoiceId={} recipientUserId={}",
                                invoice.getInvoiceId(), msg.getUserId());

                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                "notification.invoice.generated",
                                msg);
        }

        // -------------------------------------------------------------------------
        // Internal — Helpers
        // -------------------------------------------------------------------------

        private Invoice resolveInvoice(String invoiceId, String tenantId) {
                return invoiceRepository.findByInvoiceIdAndTenantTenantId(invoiceId, tenantId)
                                .orElseThrow(() -> {
                                        log.warn("[Invoice] Invoice not found | invoiceId={} tenantId={}", invoiceId,
                                                        tenantId);
                                        return new InvoiceNotFoundException("Invoice not found: " + invoiceId);
                                });
        }

        private Client resolveClient(String clientId, String tenantId) {
                return clientRepository.findByClientIdAndTenantTenantId(clientId, tenantId)
                                .orElseThrow(() -> {
                                        log.warn("[Invoice] Client not found | clientId={} tenantId={}", clientId,
                                                        tenantId);
                                        return new ClientNotFoundException("Client not found: " + clientId);
                                });
        }
}