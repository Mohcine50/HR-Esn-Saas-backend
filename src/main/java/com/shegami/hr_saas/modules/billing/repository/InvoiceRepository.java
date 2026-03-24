package com.shegami.hr_saas.modules.billing.repository;

import com.shegami.hr_saas.modules.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    Optional<Invoice> findByInvoiceNumberAndTenantTenantId(String invoiceNumber, String tenantId);
}
