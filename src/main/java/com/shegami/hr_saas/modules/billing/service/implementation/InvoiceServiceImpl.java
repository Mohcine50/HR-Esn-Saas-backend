package com.shegami.hr_saas.modules.billing.service.implementation;

import com.shegami.hr_saas.config.domain.rabbitMq.RabbitMQConfig;
import com.shegami.hr_saas.modules.billing.entity.Invoice;
import com.shegami.hr_saas.modules.billing.entity.InvoiceLine;
import com.shegami.hr_saas.modules.billing.enums.InvoiceStatus;
import com.shegami.hr_saas.modules.billing.repository.InvoiceRepository;
import com.shegami.hr_saas.modules.billing.service.InvoiceService;
import com.shegami.hr_saas.modules.billing.service.PdfGeneratorService;
import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.modules.timesheet.dto.TimesheetApprovedEvent;
import com.shegami.hr_saas.modules.timesheet.entity.Timesheet;
import com.shegami.hr_saas.modules.timesheet.entity.TimesheetEntry;
import com.shegami.hr_saas.modules.timesheet.repository.TimesheetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final TimesheetRepository timesheetRepository;
    private final PdfGeneratorService pdfGeneratorService;

    @RabbitListener(queues = RabbitMQConfig.BILLING_QUEUE)
    @Transactional
    @Override
    public void handleTimesheetApproved(TimesheetApprovedEvent event) {
        log.info("[Billing] Received TimesheetApprovedEvent for Timesheet ID: {}", event.getTimesheetId());

        Timesheet timesheet = timesheetRepository.findByIdAndTenant(event.getTimesheetId(), event.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Timesheet not found: " + event.getTimesheetId()));

        // Check if invoice already exists for this timesheet (Idempotency)
        // Since we don't have a direct findByTimesheet trick yet, let's just generate a unique reference
        String invoiceRef = "INV-" + timesheet.getYear() + "-" + String.format("%02d", timesheet.getMonth()) + "-" + event.getTimesheetId().substring(0, 5);

        Invoice invoice = new Invoice();
        invoice.setTenant(timesheet.getTenant());
        invoice.setCreatedAt(java.time.LocalDateTime.now());
        invoice.setCreatedBy(timesheet.getValidatedBy() != null ? timesheet.getValidatedBy().getUser() : null);
        invoice.setInvoiceNumber(invoiceRef);
        
        Client client = timesheet.getMission().getClient();
        invoice.setClient(client);
        if (client != null) {
            invoice.setClientNameAtBilling(client.getFullName());
            invoice.setClientAddressAtBilling(client.getAddress() != null ? client.getAddress() : "Address not provided");
            invoice.setVatNumberAtBilling(client.getVatNumber() != null ? client.getVatNumber() : "N/A");
        } else {
            invoice.setClientNameAtBilling("Unknown Client");
        }

        invoice.setIssueDate(LocalDate.now());
        // Due 30 days from end of the timesheet month
        YearMonth ym = YearMonth.of(timesheet.getYear(), timesheet.getMonth());
        invoice.setDueDate(ym.atEndOfMonth().plusDays(30));
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setTimesheet(timesheet);

        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal defaultTjm = BigDecimal.valueOf(500.00); // FIXME: Fetch from Consultant Contract or Mission Role RateCard

        for (TimesheetEntry entry : timesheet.getEntries()) {
            if (entry.getQuantity() > 0) {
                InvoiceLine line = new InvoiceLine();
                line.setTenant(timesheet.getTenant());
                line.setInvoice(invoice);
                line.setDescription("Consulting (" + entry.getDate() + ") - " + timesheet.getMission().getTitle());
                line.setQuantity(BigDecimal.valueOf(entry.getQuantity()));
                line.setUnitPrice(defaultTjm);
                
                BigDecimal lineTotal = line.getQuantity().multiply(line.getUnitPrice());
                line.setTotalLineAmount(lineTotal);
                
                invoice.getInvoiceLines().add(line);
                subTotal = subTotal.add(lineTotal);
            }
        }

        invoice.setSubTotal(subTotal);
        // Assuming 20% VAT
        BigDecimal vatRate = BigDecimal.valueOf(0.20);
        BigDecimal vatAmount = subTotal.multiply(vatRate).setScale(2, RoundingMode.HALF_UP);
        invoice.setVatAmount(vatAmount);
        
        invoice.setTotalAmount(subTotal.add(vatAmount));

        // Save Invoice and Cascade Lines
        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("[Billing] Invoice created with ID: {} and Amount: {}", savedInvoice.getInvoiceId(), savedInvoice.getTotalAmount());

        // Generate PDF
        try {
            byte[] pdfBytes = pdfGeneratorService.generateInvoicePdf(savedInvoice);
            // TODO: Upload pdfBytes via UploadService to S3 and save the pdfS3Key to the Invoice.
            log.info("[Billing] Successfully generated PDF for Invoice ID: {} (Size: {} bytes)", savedInvoice.getInvoiceId(), pdfBytes.length);
        } catch (Exception e) {
            log.error("[Billing] Failed to generate PDF for Invoice ID: {}", savedInvoice.getInvoiceId(), e);
        }
    }
}
