package com.shegami.hr_saas.modules.reporting.service.implementation;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.billing.entity.Invoice;
import com.shegami.hr_saas.modules.billing.repository.InvoiceRepository;
import com.shegami.hr_saas.modules.billing.repository.PaymentRepository;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.mission.entity.Mission;
import com.shegami.hr_saas.modules.mission.enums.ConsultantStatus;
import com.shegami.hr_saas.modules.mission.enums.MissionStatus;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.mission.repository.MissionRepository;
import com.shegami.hr_saas.modules.reporting.dto.reports.*;
import com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.MonthlyUtilizationDto;
import com.shegami.hr_saas.modules.timesheet.repository.TimesheetRepository;
import com.shegami.hr_saas.shared.model.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ConsultantRepository consultantRepository;
    @Mock
    private MissionRepository missionRepository;
    @Mock
    private TimesheetRepository timesheetRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private final String TENANT_ID = "test-tenant";

    @BeforeEach
    void setUp() {
        UserContext userContext = new UserContext("user-1", TENANT_ID, "test@test.com", "token");
        UserContextHolder.setCurrentUserContext(userContext);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clearCurrentUserContext();
    }

    @Test
    @DisplayName("Workforce Report - Should calculate utilization rate correctly")
    void testGetWorkforceReport() {
        // Arrange
        when(consultantRepository.countByTenantTenantId(TENANT_ID)).thenReturn(10L);
        when(consultantRepository.countByStatusAndTenantId(TENANT_ID)).thenReturn(List.of(
                new CountByStatusDto(ConsultantStatus.AVAILABLE.name(), 3L),
                new CountByStatusDto(ConsultantStatus.ON_MISSION.name(), 7L)));
        when(consultantRepository.countBySeniorityAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(consultantRepository.countByTypeAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(consultantRepository.averageInternalDailyCostByTenantId(TENANT_ID)).thenReturn(new BigDecimal("500.00"));
        when(missionRepository.findAllByTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(consultantRepository.findAllByTenantId(TENANT_ID)).thenReturn(Collections.emptyList());

        // Act
        WorkforceReportDto report = reportService.getWorkforceReport(null, null);

        // Assert
        assertNotNull(report);
        assertEquals(70.0, report.utilizationRate());
        assertEquals(10L, report.totalConsultants());
        assertEquals(3L, report.benchedConsultantsCount());
        assertEquals(new BigDecimal("500.00"), report.averageInternalDailyCost());
    }

    @Test
    @DisplayName("Revenue Report - Should calculate growth percentage correctly")
    void testGetRevenueReport() {
        // Arrange
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(12);

        when(invoiceRepository.sumTotalAmountByStatusInAndIssueDateBetweenAndTenantId(anyList(), any(), any(),
                eq(TENANT_ID)))
                .thenReturn(new BigDecimal("10000.00")) // Current period
                .thenReturn(new BigDecimal("8000.00")); // Previous period

        when(invoiceRepository.averagePaidInvoiceAmountByTenantId(TENANT_ID)).thenReturn(new BigDecimal("1000.00"));
        when(invoiceRepository.findMonthlyRevenueTrend(any(), eq(TENANT_ID))).thenReturn(new ArrayList<>());
        when(invoiceRepository.findTopClientsByRevenue(eq(TENANT_ID), any())).thenReturn(new ArrayList<>());
        when(invoiceRepository.findRevenueByProject(eq(TENANT_ID), any())).thenReturn(new ArrayList<>());

        // Act
        RevenueReportDto report = reportService.getRevenueReport(start, end);

        // Assert
        assertNotNull(report);
        assertEquals(new BigDecimal("10000.00"), report.totalRevenue());
        assertEquals(new BigDecimal("8000.00"), report.totalRevenueLastPeriod());
        assertEquals(25.0, report.revenueGrowthPercentage());
    }

    @Test
    @DisplayName("Mission Report - Should calculate completion rate correctly")
    void testGetMissionReport() {
        // Arrange
        when(missionRepository.countByStatusAndTenantId(TENANT_ID)).thenReturn(List.of(
                new CountByStatusDto(MissionStatus.COMPLETED.name(), 5L),
                new CountByStatusDto(MissionStatus.ACTIVE.name(), 5L)));
        when(missionRepository.countByTenantTenantIdAndStatusIn(eq(TENANT_ID), anyList())).thenReturn(5L);
        when(missionRepository.countByTenantTenantIdAndStatus(TENANT_ID, MissionStatus.COMPLETED)).thenReturn(4L);
        when(missionRepository.countByTenantTenantIdAndStatus(TENANT_ID, MissionStatus.CLOSED)).thenReturn(1L);
        when(missionRepository.findAllByTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(missionRepository.countByPriorityAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(missionRepository.countStartedByMonthAndTenantId(any(), eq(TENANT_ID)))
                .thenReturn(Collections.emptyList());
        when(missionRepository.countByClientAndTenantId(eq(TENANT_ID), any())).thenReturn(Collections.emptyList());

        // Act
        MissionReportDto report = reportService.getMissionReport(null, null);

        // Assert
        assertNotNull(report);
        assertEquals(10L, report.totalMissions());
        assertEquals(5L, report.activeMissions());
        assertEquals(5L, report.completedMissions());
        assertEquals(50.0, report.missionCompletionRate());
    }

    @Test
    @DisplayName("Billing Report - Should calculate DSO and aging buckets correctly")
    void testGetBillingReport() {
        // Arrange
        when(invoiceRepository.sumTotalInvoicedByTenantId(TENANT_ID)).thenReturn(new BigDecimal("10000.00"));
        when(invoiceRepository.sumPaidAmountByTenantId(TENANT_ID)).thenReturn(new BigDecimal("7000.00"));
        when(invoiceRepository.sumTotalAmountByStatusInAndTenantId(anyList(), eq(TENANT_ID)))
                .thenReturn(new BigDecimal("3000.00")) // Outstanding
                .thenReturn(new BigDecimal("1000.00")); // Overdue

        // DSO Setup
        Invoice inv = new Invoice();
        inv.setIssueDate(LocalDate.now().minusDays(30));
        inv.setTotalAmount(new BigDecimal("1000.00"));
        when(invoiceRepository.findAllSentAndOverdueByTenantId(TENANT_ID)).thenReturn(List.of(inv));

        when(invoiceRepository.countByStatusAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(invoiceRepository.findMonthlyInvoicedTrend(any(), eq(TENANT_ID))).thenReturn(Collections.emptyList());
        when(paymentRepository.countByMethodAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());

        // Act
        BillingReportDto report = reportService.getBillingReport(null, null);

        // Assert
        assertNotNull(report);
        assertEquals(70.0, report.collectionRate());
        assertEquals(30.0, report.averageDaysSalesOutstanding());
        assertEquals(1, report.invoiceAgingBuckets().get(0).invoiceCount()); // 0-30 days
        assertEquals(new BigDecimal("1000.00"), report.invoiceAgingBuckets().get(0).totalAmount());
    }

    @Test
    @DisplayName("Timesheet Report - Should calculate submission and approval rates correctly")
    void testGetTimesheetReport() {
        // Arrange
        when(timesheetRepository.countByTenantTenantId(TENANT_ID)).thenReturn(20L);
        when(timesheetRepository.countByTenantTenantIdAndStatus(TENANT_ID,
                com.shegami.hr_saas.modules.timesheet.enums.TimesheetStatus.SUBMITTED)).thenReturn(5L);
        when(timesheetRepository.countByTenantTenantIdAndStatus(TENANT_ID,
                com.shegami.hr_saas.modules.timesheet.enums.TimesheetStatus.APPROVED)).thenReturn(5L);
        when(timesheetRepository.countByTenantTenantIdAndStatus(TENANT_ID,
                com.shegami.hr_saas.modules.timesheet.enums.TimesheetStatus.REJECTED)).thenReturn(5L);
        when(timesheetRepository.countByTenantTenantIdAndStatus(TENANT_ID,
                com.shegami.hr_saas.modules.timesheet.enums.TimesheetStatus.INVOICED)).thenReturn(5L);

        when(timesheetRepository.countByStatusAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(timesheetRepository.countSubmissionsByMonthAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(timesheetRepository.findTopConsultantsByHours(eq(TENANT_ID), any())).thenReturn(Collections.emptyList());

        // Act
        TimesheetReportDto report = reportService.getTimesheetReport(null, null);

        // Assert
        assertNotNull(report);
        assertEquals(20L, report.totalTimesheets());
        assertEquals(100.0, report.submissionRate()); // totalProcessed = 20 / totalTimesheets = 20
        assertEquals(50.0, report.approvalRate()); // (approved + invoiced) = 10 / totalProcessed = 20
        assertEquals(25.0, report.rejectionRate()); // rejected = 5 / totalProcessed = 20
    }

    @Test
    @DisplayName("Workforce Report - Should handle zero consultants scenario")
    void testGetWorkforceReport_ZeroConsultants() {
        // Arrange
        when(consultantRepository.countByTenantTenantId(TENANT_ID)).thenReturn(0L);
        when(consultantRepository.countByStatusAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(consultantRepository.countBySeniorityAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(consultantRepository.countByTypeAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(consultantRepository.averageInternalDailyCostByTenantId(TENANT_ID)).thenReturn(null);

        // Act
        WorkforceReportDto report = reportService.getWorkforceReport(null, null);

        // Assert
        assertNotNull(report);
        assertEquals(0.0, report.utilizationRate());
        assertEquals(0L, report.totalConsultants());
        assertTrue(BigDecimal.ZERO.compareTo(report.averageInternalDailyCost()) == 0);
        assertTrue(report.monthlyUtilization().isEmpty());
    }

    @Test
    @DisplayName("Workforce Report - Should calculate monthly utilization correctly")
    void testGetWorkforceReport_WithMonthlyUtilization() {
        // Arrange
        when(consultantRepository.countByTenantTenantId(TENANT_ID)).thenReturn(1L);
        when(consultantRepository.averageInternalDailyCostByTenantId(TENANT_ID)).thenReturn(BigDecimal.TEN);

        Consultant c1 = new Consultant();
        c1.setConsultantId("clt-1");

        Mission m1 = new Mission();
        m1.setStartDate(LocalDate.now().minusMonths(1).withDayOfMonth(1));
        m1.setEndDate(LocalDate.now().plusMonths(1));
        m1.setConsultants(java.util.Set.of(c1));

        when(missionRepository.findAllByTenantId(TENANT_ID)).thenReturn(List.of(m1));
        when(consultantRepository.countByStatusAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(consultantRepository.countBySeniorityAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(consultantRepository.countByTypeAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(consultantRepository.findAllByTenantId(TENANT_ID)).thenReturn(Collections.emptyList());

        // Act
        WorkforceReportDto report = reportService.getWorkforceReport(null, null);

        // Assert
        assertNotNull(report);
        assertFalse(report.monthlyUtilization().isEmpty());
        // Last month should have 100% utilization since 1/1 consultants are active
        MonthlyUtilizationDto lastMonth = report.monthlyUtilization().get(10); // index 10 is last month (i=1)
        assertEquals(100.0, lastMonth.utilizationRate());
    }

    @Test
    @DisplayName("Workforce Report - Should identify top skills correctly")
    void testGetWorkforceReport_WithTopSkills() {
        // Arrange
        when(consultantRepository.countByTenantTenantId(TENANT_ID)).thenReturn(2L);
        when(consultantRepository.averageInternalDailyCostByTenantId(TENANT_ID)).thenReturn(BigDecimal.TEN);

        Consultant c1 = new Consultant();
        c1.setSkills(java.util.Set.of("Java", "Spring"));
        Consultant c2 = new Consultant();
        c2.setSkills(java.util.Set.of("Java", "AWS"));

        when(consultantRepository.findAllByTenantId(TENANT_ID)).thenReturn(List.of(c1, c2));
        when(missionRepository.findAllByTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(consultantRepository.countByStatusAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(consultantRepository.countBySeniorityAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(consultantRepository.countByTypeAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());

        // Act
        WorkforceReportDto report = reportService.getWorkforceReport(null, null);

        // Assert
        assertNotNull(report);
        assertEquals(3, report.topSkills().size());
        assertEquals("Java", report.topSkills().get(0).skill());
        assertEquals(2, report.topSkills().get(0).count());
    }
}
