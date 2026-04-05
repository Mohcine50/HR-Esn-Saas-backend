package com.shegami.hr_saas.modules.billing.repository;

import com.shegami.hr_saas.modules.billing.entity.Invoice;
import com.shegami.hr_saas.modules.billing.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
        Optional<Invoice> findByInvoiceNumberAndTenantTenantId(String invoiceNumber, String tenantId);

        Optional<Invoice> findByInvoiceIdAndTenantTenantId(String invoiceId, String tenantId);

        Page<Invoice> findAllByTenantTenantId(String tenantId, Pageable pageable);

        Page<Invoice> findAllByTimesheetConsultantConsultantIdAndTenantTenantId(String consultantId, String tenantId,
                        Pageable pageable);

        Page<Invoice> findAllByTimesheetConsultantConsultantIdAndStatusAndTenantTenantId(String consultantId,
                        InvoiceStatus status, String tenantId, Pageable pageable);

        boolean existsByTimesheetTimesheetId(String timesheetId);

        @org.springframework.data.jpa.repository.Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.tenant.tenantId = :tenantId AND i.status IN :statuses")
        java.math.BigDecimal sumTotalAmountByStatusInAndTenantId(
                        @org.springframework.data.repository.query.Param("statuses") java.util.List<InvoiceStatus> statuses,
                        @org.springframework.data.repository.query.Param("tenantId") String tenantId);

        @org.springframework.data.jpa.repository.Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.tenant.tenantId = :tenantId AND i.status IN :statuses AND i.issueDate >= :startDate AND i.issueDate <= :endDate")
        java.math.BigDecimal sumTotalAmountByStatusInAndIssueDateBetweenAndTenantId(
                        @org.springframework.data.repository.query.Param("statuses") java.util.List<InvoiceStatus> statuses,
                        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
                        @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate,
                        @org.springframework.data.repository.query.Param("tenantId") String tenantId);

        @org.springframework.data.jpa.repository.Query("SELECT AVG(i.totalAmount) FROM Invoice i WHERE i.tenant.tenantId = :tenantId AND i.status = 'PAID'")
        java.math.BigDecimal averagePaidInvoiceAmountByTenantId(
                        @org.springframework.data.repository.query.Param("tenantId") String tenantId);

        long countByTenantTenantId(String tenantId);

        @org.springframework.data.jpa.repository.Query("SELECT new com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto(CAST(i.status AS string), COUNT(i)) FROM Invoice i WHERE i.tenant.tenantId = :tenantId GROUP BY i.status")
        java.util.List<com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto> countByStatusAndTenantId(
                        @org.springframework.data.repository.query.Param("tenantId") String tenantId);

        long countByTenantTenantIdAndStatus(String tenantId, InvoiceStatus status);

        java.util.List<Invoice> findTop5ByTimesheetConsultantConsultantIdAndTenantTenantIdOrderByCreatedAtDesc(
                        String consultantId, String tenantId);

        @org.springframework.data.jpa.repository.Query("SELECT new com.shegami.hr_saas.modules.reporting.dto.shared.RevenueByClientDto(c.fullName, SUM(i.totalAmount)) FROM Invoice i JOIN i.client c WHERE i.tenant.tenantId = :tenantId AND i.status = 'PAID' GROUP BY c.fullName ORDER BY SUM(i.totalAmount) DESC")
        java.util.List<com.shegami.hr_saas.modules.reporting.dto.shared.RevenueByClientDto> findTopClientsByRevenue(
                        @org.springframework.data.repository.query.Param("tenantId") String tenantId,
                        Pageable pageable);

        @org.springframework.data.jpa.repository.Query("SELECT new com.shegami.hr_saas.modules.reporting.dto.shared.MonthlyRevenueDto(MONTH(i.issueDate), YEAR(i.issueDate), SUM(i.totalAmount)) FROM Invoice i WHERE i.tenant.tenantId = :tenantId AND i.status = 'PAID' AND i.issueDate >= :startDate GROUP BY YEAR(i.issueDate), MONTH(i.issueDate) ORDER BY YEAR(i.issueDate) ASC, MONTH(i.issueDate) ASC")
        java.util.List<com.shegami.hr_saas.modules.reporting.dto.shared.MonthlyRevenueDto> findMonthlyRevenueTrend(
                        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
                        @org.springframework.data.repository.query.Param("tenantId") String tenantId);

        // ── Analytics / Reports queries ──────────────────────────────────────

        @org.springframework.data.jpa.repository.Query("SELECT i FROM Invoice i WHERE i.tenant.tenantId = :tenantId AND i.status IN ('SENT', 'OVERDUE')")
        java.util.List<Invoice> findAllSentAndOverdueByTenantId(
                        @org.springframework.data.repository.query.Param("tenantId") String tenantId);

        @org.springframework.data.jpa.repository.Query("SELECT new com.shegami.hr_saas.modules.reporting.dto.shared.RevenueByProjectDto(p.name, SUM(i.totalAmount)) FROM Invoice i JOIN i.timesheet t JOIN t.mission m JOIN m.project p WHERE i.tenant.tenantId = :tenantId AND i.status = 'PAID' GROUP BY p.name ORDER BY SUM(i.totalAmount) DESC")
        java.util.List<com.shegami.hr_saas.modules.reporting.dto.shared.RevenueByProjectDto> findRevenueByProject(
                        @org.springframework.data.repository.query.Param("tenantId") String tenantId,
                        Pageable pageable);

        @org.springframework.data.jpa.repository.Query("SELECT new com.shegami.hr_saas.modules.reporting.dto.shared.MonthlyRevenueDto(MONTH(i.issueDate), YEAR(i.issueDate), SUM(i.totalAmount)) FROM Invoice i WHERE i.tenant.tenantId = :tenantId AND i.issueDate >= :startDate GROUP BY YEAR(i.issueDate), MONTH(i.issueDate) ORDER BY YEAR(i.issueDate) ASC, MONTH(i.issueDate) ASC")
        java.util.List<com.shegami.hr_saas.modules.reporting.dto.shared.MonthlyRevenueDto> findMonthlyInvoicedTrend(
                        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
                        @org.springframework.data.repository.query.Param("tenantId") String tenantId);

        @org.springframework.data.jpa.repository.Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.tenant.tenantId = :tenantId AND i.status = 'PAID'")
        java.math.BigDecimal sumPaidAmountByTenantId(
                        @org.springframework.data.repository.query.Param("tenantId") String tenantId);

        @org.springframework.data.jpa.repository.Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.tenant.tenantId = :tenantId")
        java.math.BigDecimal sumTotalInvoicedByTenantId(
                        @org.springframework.data.repository.query.Param("tenantId") String tenantId);
}
