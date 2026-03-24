package com.shegami.hr_saas.modules.billing.entity;

import com.shegami.hr_saas.modules.billing.enums.InvoiceStatus;
import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.modules.upload.entity.UploadFile;
import com.shegami.hr_saas.modules.timesheet.entity.Timesheet;
import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter @Setter
public class Invoice extends BaseTenantEntity {

    @Column(unique = true, nullable = false)
    private String invoiceNumber; // INV-2026-001

    @ManyToOne(fetch = FetchType.LAZY)
    private Client client;

    private String clientNameAtBilling;
    private String clientAddressAtBilling;
    private String vatNumberAtBilling;

    @Column(precision = 12, scale = 2)
    private BigDecimal subTotal;

    @Column(precision = 12, scale = 2)
    private BigDecimal vatAmount;

    @OneToOne
    private UploadFile invoiceFile;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalAmount;

    private LocalDate issueDate;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    private String pdfS3Key;

    @ManyToOne(fetch = FetchType.LAZY)
    private Timesheet timesheet;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<InvoiceLine> invoiceLines = new HashSet<>();

    @Id
    @Column(name = "invoice_id", nullable = false)
    private String invoiceId;
    @PrePersist
    public void generateInvoiceId() {
        if (this.invoiceId == null) {
            this.invoiceId = "INV-" + UUID.randomUUID();
        }
    }
}
