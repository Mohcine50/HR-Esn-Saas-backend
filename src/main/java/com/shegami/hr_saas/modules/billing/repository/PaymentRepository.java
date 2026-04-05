package com.shegami.hr_saas.modules.billing.repository;

import com.shegami.hr_saas.modules.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByInvoiceInvoiceIdAndTenantTenantId(String invoiceId, String tenantId);

    List<Payment> findTop10ByTenantTenantIdOrderByPaymentDateDesc(String tenantId);

    // ── Analytics / Reports queries ──────────────────────────────────────

    @org.springframework.data.jpa.repository.Query("SELECT new com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto(CAST(p.method AS string), COUNT(p)) FROM Payment p WHERE p.tenant.tenantId = :tenantId GROUP BY p.method")
    java.util.List<com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto> countByMethodAndTenantId(
            @org.springframework.data.repository.query.Param("tenantId") String tenantId);
}
