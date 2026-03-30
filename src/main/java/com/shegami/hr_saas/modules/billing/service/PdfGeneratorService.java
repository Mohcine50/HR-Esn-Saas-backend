package com.shegami.hr_saas.modules.billing.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.shegami.hr_saas.modules.billing.entity.Invoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGeneratorService {

    private final SpringTemplateEngine templateEngine;

    public byte[] generateInvoicePdf(Invoice invoice) {
        log.info("Generating PDF for Invoice ID: {}", invoice.getInvoiceId());

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Context context = new Context();
            context.setVariable("invoice", invoice);

            // Render HTML using Thymeleaf
            String htmlContent = templateEngine.process("invoice", context);
            log.info("HTML content: {}", htmlContent);
            // Convert HTML to PDF using OpenHTMLToPDF
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, "classpath:/templates/");
            builder.toStream(os);
            builder.run();

            return os.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF for Invoice ID: {}", invoice.getInvoiceId(), e);
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
