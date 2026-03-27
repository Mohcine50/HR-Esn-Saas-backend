package com.shegami.hr_saas.modules.billing.repository;

import com.shegami.hr_saas.modules.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByInvoiceInvoiceIdAndTenantTenantId(String invoiceId, String tenantId);
}
