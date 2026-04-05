package com.shegami.hr_saas.modules.reporting.service.implementation;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.billing.enums.InvoiceStatus;
import com.shegami.hr_saas.modules.billing.repository.InvoiceRepository;
import com.shegami.hr_saas.modules.billing.repository.PaymentRepository;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.hr.enums.InvitationStatus;
import com.shegami.hr_saas.modules.hr.repository.EmployeeRepository;
import com.shegami.hr_saas.modules.hr.repository.InvitationRepository;
import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.mission.enums.MissionStatus;
import com.shegami.hr_saas.modules.mission.enums.ProjectStatus;
import com.shegami.hr_saas.modules.mission.repository.ClientRepository;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.mission.repository.MissionActivityRepository;
import com.shegami.hr_saas.modules.mission.repository.MissionRepository;
import com.shegami.hr_saas.modules.mission.repository.ProjectRepository;
import com.shegami.hr_saas.modules.notifications.repository.NotificationRepository;
import com.shegami.hr_saas.modules.reporting.dto.AdminDashboardDto;
import com.shegami.hr_saas.modules.reporting.dto.ConsultantDashboardDto;
import com.shegami.hr_saas.modules.reporting.dto.FinancialDashboardDto;
import com.shegami.hr_saas.modules.reporting.dto.ManagerDashboardDto;
import com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto;
import com.shegami.hr_saas.modules.timesheet.repository.TimesheetEntryRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private MissionRepository missionRepository;
    @Mock
    private ConsultantRepository consultantRepository;
    @Mock
    private TimesheetRepository timesheetRepository;
    @Mock
    private TimesheetEntryRepository timesheetEntryRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private MissionActivityRepository missionActivityRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private final String TENANT_ID = "test-tenant";
    private final String USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        UserContext userContext = new UserContext(USER_ID, TENANT_ID, "test@test.com", "token");
        UserContextHolder.setCurrentUserContext(userContext);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clearCurrentUserContext();
    }

    @Test
    @DisplayName("Consultant Dashboard - Should retrieve all metrics correctly")
    void testGetConsultantDashboard() {
        // Arrange
        Consultant consultant = new Consultant();
        consultant.setConsultantId("clt-1");

        Client client = new Client();
        client.setFullName("Client Alpha");

        when(consultantRepository.findByUserUserId(USER_ID)).thenReturn(Optional.of(consultant));
        when(missionRepository.findByConsultantAndStatusInAndTenantId(eq(consultant), anyList(), eq(TENANT_ID)))
                .thenReturn(Collections.emptyList());
        when(timesheetEntryRepository.sumQuantityByConsultantAndMonthAndYear(eq("clt-1"), anyInt(), anyInt()))
                .thenReturn(15.5);
        when(invoiceRepository.findTop5ByTimesheetConsultantConsultantIdAndTenantTenantIdOrderByCreatedAtDesc(
                eq("clt-1"), eq(TENANT_ID)))
                .thenReturn(Collections.emptyList());
        when(notificationRepository.countUnreadByUserId(USER_ID)).thenReturn(5L);
        when(missionRepository.findUpcomingDeadlinesForConsultant(any(), any(), eq(consultant), eq(TENANT_ID)))
                .thenReturn(Collections.emptyList());

        // Act
        ConsultantDashboardDto dashboard = dashboardService.getConsultantDashboard();

        // Assert
        assertNotNull(dashboard);
        assertEquals(15.5, dashboard.totalDaysWorkedThisMonth());
        assertEquals(5L, dashboard.unreadNotificationsCount());
        assertTrue(dashboard.activeMissions().isEmpty());
    }

    @Test
    @DisplayName("Admin Dashboard - Should retrieve aggregate counts correctly")
    void testGetAdminDashboard() {
        // Arrange
        when(employeeRepository.count()).thenReturn(50L);
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());
        when(invitationRepository.countByTenantTenantIdAndStatus(TENANT_ID, InvitationStatus.PENDING)).thenReturn(3L);
        when(consultantRepository.countByTenantTenantId(TENANT_ID)).thenReturn(20L);
        when(consultantRepository.countByStatusAndTenantId(TENANT_ID)).thenReturn(List.of(
                new CountByStatusDto("ON_MISSION", 15L)));
        when(projectRepository.countByTenantTenantIdAndProjectStatus(TENANT_ID, ProjectStatus.IN_PROGRESS))
                .thenReturn(5L);
        when(projectRepository.countByTenantTenantId(TENANT_ID)).thenReturn(10L);
        when(missionRepository.countByTenantTenantIdAndStatusIn(eq(TENANT_ID), anyList())).thenReturn(8L);
        when(missionRepository.countByStatusAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(clientRepository.countByTenantTenantId(TENANT_ID)).thenReturn(12L);
        when(missionActivityRepository.findRecentByTenantId(eq(TENANT_ID), any())).thenReturn(Collections.emptyList());

        // Act
        AdminDashboardDto dashboard = dashboardService.getAdminDashboard();

        // Assert
        assertNotNull(dashboard);
        assertEquals(50L, dashboard.totalEmployees());
        assertEquals(3L, dashboard.pendingInvitationsCount());
        assertEquals(20L, dashboard.totalConsultants());
        assertEquals(75.0, dashboard.utilizationRate()); // 15/20 * 100
        assertEquals(5L, dashboard.activeProjectsCount());
        assertEquals(12L, dashboard.totalClients());
    }

    @Test
    @DisplayName("Manager Dashboard - Should retrieve managed mission metrics correctly")
    void testGetManagerDashboard() {
        // Arrange
        Employee manager = new Employee();
        manager.setEmployeeId("mgr-1");

        when(employeeRepository.findByUserUserId(USER_ID)).thenReturn(Optional.of(manager));
        when(missionRepository.findByAccountManagerAndTenantId("mgr-1", TENANT_ID)).thenReturn(new ArrayList<>());
        when(missionRepository.countByStatusAndAccountManagerAndTenantId("mgr-1", TENANT_ID))
                .thenReturn(Collections.emptyList());
        when(missionRepository.findByAccountManagerAndStatusInAndTenantId(eq("mgr-1"), anyList(), eq(TENANT_ID)))
                .thenReturn(Collections.emptyList());
        when(timesheetRepository.findSubmittedTimesheetsForManager("mgr-1", TENANT_ID))
                .thenReturn(Collections.emptyList());
        when(consultantRepository.countForManagerAndTenantId("mgr-1", TENANT_ID)).thenReturn(10L);
        when(consultantRepository.countByStatusForManagerAndTenantId("mgr-1", TENANT_ID))
                .thenReturn(Collections.emptyList());
        when(timesheetEntryRepository.sumApprovedQuantityByManagerAndMonthAndYear(eq("mgr-1"), anyInt(), anyInt()))
                .thenReturn(20.0);
        when(missionRepository.findUpcomingDeadlinesForManager(any(), any(), eq("mgr-1"), eq(TENANT_ID)))
                .thenReturn(Collections.emptyList());
        when(missionActivityRepository.findRecentByManagerAndTenantId(eq("mgr-1"), eq(TENANT_ID), any()))
                .thenReturn(Collections.emptyList());

        // Act
        ManagerDashboardDto dashboard = dashboardService.getManagerDashboard();

        // Assert
        assertNotNull(dashboard);
        assertEquals(10L, dashboard.teamConsultantsCount());
        assertEquals(20.0, dashboard.totalApprovedDaysThisMonth());
    }

    @Test
    @DisplayName("Financial Dashboard - Should calculate revenue and outstanding amounts correctly")
    void testGetFinancialDashboard() {
        // Arrange
        when(invoiceRepository.sumTotalAmountByStatusInAndIssueDateBetweenAndTenantId(anyList(), any(), any(),
                eq(TENANT_ID)))
                .thenReturn(new BigDecimal("5000.00")) // Month
                .thenReturn(new BigDecimal("50000.00")); // Year

        when(invoiceRepository.sumTotalAmountByStatusInAndTenantId(anyList(), eq(TENANT_ID)))
                .thenReturn(new BigDecimal("15000.00")) // Outstanding
                .thenReturn(new BigDecimal("2000.00")); // Overdue

        when(invoiceRepository.averagePaidInvoiceAmountByTenantId(TENANT_ID)).thenReturn(new BigDecimal("1000.00"));
        when(invoiceRepository.countByTenantTenantId(TENANT_ID)).thenReturn(100L);
        when(invoiceRepository.countByStatusAndTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        when(invoiceRepository.countByTenantTenantIdAndStatus(TENANT_ID, InvoiceStatus.OVERDUE)).thenReturn(2L);
        when(paymentRepository.findTop10ByTenantTenantIdOrderByPaymentDateDesc(TENANT_ID))
                .thenReturn(Collections.emptyList());
        when(invoiceRepository.findTopClientsByRevenue(eq(TENANT_ID), any())).thenReturn(Collections.emptyList());
        when(timesheetRepository.findApprovedTimesheetsForMonth(eq(TENANT_ID), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(invoiceRepository.findMonthlyRevenueTrend(any(), eq(TENANT_ID))).thenReturn(Collections.emptyList());

        // Act
        FinancialDashboardDto dashboard = dashboardService.getFinancialDashboard();

        // Assert
        assertNotNull(dashboard);
        assertTrue(new BigDecimal("5000.00").compareTo(dashboard.totalRevenueThisMonth()) == 0);
        assertTrue(new BigDecimal("50000.00").compareTo(dashboard.totalRevenueThisYear()) == 0);
        assertTrue(new BigDecimal("15000.00").compareTo(dashboard.outstandingAmount()) == 0);
        assertEquals(100L, dashboard.totalInvoices());
        assertEquals(2L, dashboard.overdueInvoicesCount());
    }
}
