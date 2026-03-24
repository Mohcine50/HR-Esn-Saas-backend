package com.shegami.hr_saas.modules.billing.entity;

import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "invoice_lines")
@Getter
@Setter
public class InvoiceLine extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_invoice_id")
    private Invoice invoice;

    @Column(nullable = false)
    private String description;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal quantity;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal totalLineAmount;

    @Id
    @Column(name = "invoice_line_id", nullable = false)
    private String invoiceLineId;

    @PrePersist
    public void generateInvoiceLineId() {
        if (this.invoiceLineId == null) {
            this.invoiceLineId = "IVL-" + UUID.randomUUID();
        }
    }
}
