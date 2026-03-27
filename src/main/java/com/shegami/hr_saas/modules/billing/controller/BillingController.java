package com.shegami.hr_saas.modules.billing.controller;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.billing.dto.InvoiceDto;
import com.shegami.hr_saas.modules.billing.dto.PaymentRequest;
import com.shegami.hr_saas.modules.billing.dto.UpdateInvoiceStatusRequest;
import com.shegami.hr_saas.modules.billing.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final InvoiceService invoiceService;

    @GetMapping("/invoices")
    public ResponseEntity<Page<InvoiceDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(pageable));
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<InvoiceDto> getOne(@PathVariable String id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @PatchMapping("/invoices/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateInvoiceStatusRequest req) {
        invoiceService.updateStatus(id, req.getStatus());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/invoices/{id}/payments")
    public ResponseEntity<Void> recordPayment(
            @PathVariable String id,
            @Valid @RequestBody PaymentRequest req) {
        invoiceService.recordPayment(id, req);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/invoices/{id}/download")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable String id) {
        String url = invoiceService.getDownloadUrl(id);
        return ResponseEntity.ok(Map.of("url", url != null ? url : ""));
    }
}
