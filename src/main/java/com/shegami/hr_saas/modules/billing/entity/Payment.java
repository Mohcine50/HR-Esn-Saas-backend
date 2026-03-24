package com.shegami.hr_saas.modules.billing.entity;

import com.shegami.hr_saas.modules.billing.enums.PaymentMethod;
import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Invoice invoice;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    private LocalDate paymentDate;

    private String transactionReference;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;


    @Id
    @Column(name = "payment_id", nullable = false)
    private String paymentId;
    @PrePersist
    public void generatePaymentId() {
        if (this.paymentId == null) {
            this.paymentId = "PM-" + UUID.randomUUID();
        }
    }
}
