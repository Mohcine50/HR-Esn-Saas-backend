package com.shegami.hr_saas.modules.reporting.service.implementation;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.billing.enums.InvoiceStatus;
import com.shegami.hr_saas.modules.billing.repository.InvoiceRepository;
import com.shegami.hr_saas.modules.billing.repository.PaymentRepository;
import com.shegami.hr_saas.modules.hr.dto.EmployeesCountByContract;
import com.shegami.hr_saas.modules.hr.enums.InvitationStatus;
import com.shegami.hr_saas.modules.hr.repository.EmployeeRepository;
import com.shegami.hr_saas.modules.hr.repository.InvitationRepository;
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
import com.shegami.hr_saas.modules.reporting.dto.shared.*;
import com.shegami.hr_saas.modules.reporting.service.DashboardService;
import com.shegami.hr_saas.modules.timesheet.repository.TimesheetEntryRepository;
import com.shegami.hr_saas.modules.timesheet.repository.TimesheetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

        private final MissionRepository missionRepository;
        private final ConsultantRepository consultantRepository;
        private final TimesheetRepository timesheetRepository;
        private final TimesheetEntryRepository timesheetEntryRepository;
        private final InvoiceRepository invoiceRepository;
        private final NotificationRepository notificationRepository;
        private final EmployeeRepository employeeRepository;
        private final ProjectRepository projectRepository;
        private final ClientRepository clientRepository;
        private final InvitationRepository invitationRepository;
        private final MissionActivityRepository missionActivityRepository;
        private final PaymentRepository paymentRepository;

        @Override
        public ConsultantDashboardDto getConsultantDashboard() {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                String userId = UserContextHolder.getCurrentUserContext().userId();
                log.info("REST request to get Manager Dashboard for tenant {}", tenantId);
                log.info("Fetching consultant dashboard for user {} and tenant {}", userId, tenantId);

                Consultant consultant = consultantRepository.findByUserUserId(userId)
                                .orElseThrow(() -> new RuntimeException("Consultant not found for user: " + userId));
                String consultantId = consultant.getConsultantId();

                List<MissionStatus> activeStatuses = List.of(MissionStatus.ACTIVE, MissionStatus.IN_PROGRESS);
                var activeMissionsList = missionRepository.findByConsultantAndStatusInAndTenantId(consultant,
                                activeStatuses,
                                tenantId);

                List<MissionSummaryDto> activeMissions = activeMissionsList.stream()
                                .map(m -> new MissionSummaryDto(m.getMissionId(), m.getTitle(),
                                                m.getClient().getFullName(),
                                                m.getStartDate(), m.getEndDate(), m.getStatus().name()))
                                .collect(Collectors.toList());

                LocalDate now = LocalDate.now();
                int month = now.getMonthValue();
                int year = now.getYear();

                Double totalDaysWorkedThisMonth = timesheetEntryRepository.sumQuantityByConsultantAndMonthAndYear(
                                consultantId,
                                month, year);

                var recentInvoicesList = invoiceRepository
                                .findTop5ByTimesheetConsultantConsultantIdAndTenantTenantIdOrderByCreatedAtDesc(
                                                consultantId, tenantId);
                List<InvoiceSummaryDto> recentInvoices = recentInvoicesList.stream()
                                .map(i -> new InvoiceSummaryDto(i.getInvoiceId(), i.getInvoiceNumber(),
                                                i.getTotalAmount(),
                                                i.getStatus().name(), i.getIssueDate()))
                                .collect(Collectors.toList());

                long unreadNotificationsCount = notificationRepository.countUnreadByUserId(userId);

                LocalDate thirtyDaysFromNow = now.plusDays(30);
                var upcomingDeadlinesList = missionRepository.findUpcomingDeadlinesForConsultant(now, thirtyDaysFromNow,
                                consultant, tenantId);
                List<MissionDeadlineDto> upcomingDeadlines = upcomingDeadlinesList.stream()
                                .map(m -> new MissionDeadlineDto(m.getMissionId(), m.getTitle(),
                                                m.getClient().getFullName(),
                                                m.getEndDate(),
                                                java.time.temporal.ChronoUnit.DAYS.between(now, m.getEndDate())))
                                .collect(Collectors.toList());

                return new ConsultantDashboardDto(
                                activeMissions.size(),
                                activeMissions,
                                null,
                                null,
                                0,
                                totalDaysWorkedThisMonth != null ? totalDaysWorkedThisMonth : 0.0,
                                recentInvoices,
                                unreadNotificationsCount,
                                upcomingDeadlines);
        }

        @Override
        public AdminDashboardDto getAdminDashboard() {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("Fetching admin dashboard for tenant {}", tenantId);

                long totalEmployees = employeeRepository.count();

                List<CountByStatusDto> employeesByStatus = employeeRepository.findAll().stream()
                                .collect(Collectors.groupingBy(e -> e.getStatus().name(), Collectors.counting()))
                                .entrySet().stream()
                                .map(e -> new CountByStatusDto(e.getKey(), e.getValue()))
                                .collect(Collectors.toList());

                long pendingInvitationsCount = invitationRepository.countByTenantTenantIdAndStatus(tenantId,
                                InvitationStatus.PENDING);

                long totalConsultants = consultantRepository.countByTenantTenantId(tenantId);
                List<CountByStatusDto> consultantsByStatus = consultantRepository.countByStatusAndTenantId(tenantId);

                long onMissionCount = consultantsByStatus.stream().filter(c -> "ON_MISSION".equals(c.label()))
                                .mapToLong(CountByStatusDto::count).sum();
                double utilizationRate = totalConsultants > 0 ? (double) onMissionCount / totalConsultants * 100 : 0.0;

                long activeProjectsCount = projectRepository.countByTenantTenantIdAndProjectStatus(tenantId,
                                ProjectStatus.IN_PROGRESS);
                long totalProjectsCount = projectRepository.countByTenantTenantId(tenantId);
                long activeMissionsCount = missionRepository.countByTenantTenantIdAndStatusIn(tenantId,
                                List.of(MissionStatus.ACTIVE, MissionStatus.IN_PROGRESS));
                List<CountByStatusDto> missionsByStatus = missionRepository.countByStatusAndTenantId(tenantId);

                long totalClients = clientRepository.countByTenantTenantId(tenantId);

                List<RecentActivityDto> recentActivities = missionActivityRepository
                                .findRecentByTenantId(tenantId, PageRequest.of(0, 10))
                                .stream()
                                .map(a -> new RecentActivityDto(a.getType().name(), a.getMission().getTitle(),
                                                a.getDescription(),
                                                a.getCreatedAt(), a.getActorName()))
                                .collect(Collectors.toList());

                return new AdminDashboardDto(
                                totalEmployees,
                                employeeRepository.countEmployeeByContractType(tenantId),
                                employeesByStatus,
                                pendingInvitationsCount,
                                totalConsultants,
                                consultantsByStatus,
                                utilizationRate,
                                activeProjectsCount,
                                totalProjectsCount,
                                activeMissionsCount,
                                missionsByStatus,
                                totalClients,
                                0,
                                recentActivities);
        }

        @Override
        public ManagerDashboardDto getManagerDashboard() {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                String userId = UserContextHolder.getCurrentUserContext().userId();
                log.info("Fetching manager dashboard for user {} and tenant {}", userId, tenantId);

                String employeeId = employeeRepository.findByUserUserId(userId).orElseThrow().getEmployeeId();

                var managedMissions = missionRepository.findByAccountManagerAndTenantId(employeeId, tenantId);
                long managedMissionsCount = managedMissions.size();
                List<CountByStatusDto> managedMissionsByStatus = missionRepository
                                .countByStatusAndAccountManagerAndTenantId(employeeId, tenantId);

                var activeManagedMissionsList = missionRepository.findByAccountManagerAndStatusInAndTenantId(employeeId,
                                List.of(MissionStatus.ACTIVE, MissionStatus.IN_PROGRESS), tenantId);
                List<MissionSummaryDto> activeManagedMissions = activeManagedMissionsList.stream()
                                .map(m -> new MissionSummaryDto(m.getMissionId(), m.getTitle(),
                                                m.getClient().getFullName(),
                                                m.getStartDate(), m.getEndDate(), m.getStatus().name()))
                                .collect(Collectors.toList());

                var pendingTimesheetsList = timesheetRepository.findSubmittedTimesheetsForManager(employeeId, tenantId);
                List<TimesheetApprovalDto> pendingTimesheets = pendingTimesheetsList.stream()
                                .map(t -> new TimesheetApprovalDto(t.getTimesheetId(),
                                                t.getConsultant().getFirstName() + " "
                                                                + t.getConsultant().getLastName(),
                                                t.getMission().getTitle(), t.getMonth(), t.getYear()))
                                .collect(Collectors.toList());

                long teamConsultantsCount = consultantRepository.countForManagerAndTenantId(employeeId, tenantId);
                List<CountByStatusDto> teamConsultantsByStatus = consultantRepository
                                .countByStatusForManagerAndTenantId(employeeId, tenantId);

                LocalDate now = LocalDate.now();
                Double totalApprovedDaysThisMonth = timesheetEntryRepository
                                .sumApprovedQuantityByManagerAndMonthAndYear(employeeId, now.getMonthValue(),
                                                now.getYear());

                LocalDate thirtyDaysFromNow = now.plusDays(30);
                var upcomingMissionEndsList = missionRepository.findUpcomingDeadlinesForManager(now, thirtyDaysFromNow,
                                employeeId, tenantId);
                List<MissionDeadlineDto> upcomingMissionEnds = upcomingMissionEndsList.stream()
                                .map(m -> new MissionDeadlineDto(m.getMissionId(), m.getTitle(),
                                                m.getClient().getFullName(),
                                                m.getEndDate(),
                                                java.time.temporal.ChronoUnit.DAYS.between(now, m.getEndDate())))
                                .collect(Collectors.toList());

                List<RecentActivityDto> recentMissionActivities = missionActivityRepository
                                .findRecentByManagerAndTenantId(employeeId, tenantId, PageRequest.of(0, 10))
                                .stream()
                                .map(a -> new RecentActivityDto(a.getType().name(), a.getMission().getTitle(),
                                                a.getDescription(),
                                                a.getCreatedAt(), a.getActorName()))
                                .collect(Collectors.toList());

                return new ManagerDashboardDto(
                                managedMissionsCount,
                                managedMissionsByStatus,
                                activeManagedMissions,
                                pendingTimesheets.size(),
                                pendingTimesheets,
                                teamConsultantsCount,
                                teamConsultantsByStatus,
                                totalApprovedDaysThisMonth != null ? totalApprovedDaysThisMonth : 0.0,
                                0,
                                upcomingMissionEnds,
                                recentMissionActivities);
        }

        @Override
        public FinancialDashboardDto getFinancialDashboard() {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("Fetching financial dashboard for tenant {}", tenantId);

                LocalDate now = LocalDate.now();
                LocalDate startOfMonth = now.withDayOfMonth(1);
                LocalDate startOfYear = now.withDayOfYear(1);

                BigDecimal totalRevenueThisMonth = invoiceRepository
                                .sumTotalAmountByStatusInAndIssueDateBetweenAndTenantId(
                                                List.of(InvoiceStatus.PAID), startOfMonth, now, tenantId);
                if (totalRevenueThisMonth == null)
                        totalRevenueThisMonth = BigDecimal.ZERO;

                BigDecimal totalRevenueThisYear = invoiceRepository
                                .sumTotalAmountByStatusInAndIssueDateBetweenAndTenantId(
                                                List.of(InvoiceStatus.PAID), startOfYear, now, tenantId);
                if (totalRevenueThisYear == null)
                        totalRevenueThisYear = BigDecimal.ZERO;

                BigDecimal outstandingAmount = invoiceRepository.sumTotalAmountByStatusInAndTenantId(
                                List.of(InvoiceStatus.SENT, InvoiceStatus.OVERDUE, InvoiceStatus.PDF_PENDING),
                                tenantId);
                if (outstandingAmount == null)
                        outstandingAmount = BigDecimal.ZERO;

                BigDecimal averageInvoiceAmount = invoiceRepository.averagePaidInvoiceAmountByTenantId(tenantId);
                if (averageInvoiceAmount == null)
                        averageInvoiceAmount = BigDecimal.ZERO;

                long totalInvoices = invoiceRepository.countByTenantTenantId(tenantId);
                List<CountByStatusDto> invoicesByStatus = invoiceRepository.countByStatusAndTenantId(tenantId);

                long overdueInvoicesCount = invoiceRepository.countByTenantTenantIdAndStatus(tenantId,
                                InvoiceStatus.OVERDUE);
                BigDecimal overdueAmount = invoiceRepository.sumTotalAmountByStatusInAndTenantId(
                                List.of(InvoiceStatus.OVERDUE),
                                tenantId);
                if (overdueAmount == null)
                        overdueAmount = BigDecimal.ZERO;

                List<PaymentSummaryDto> recentPayments = paymentRepository
                                .findTop10ByTenantTenantIdOrderByPaymentDateDesc(tenantId).stream()
                                .map(p -> new PaymentSummaryDto(p.getPaymentId(), p.getAmount(), p.getPaymentDate(),
                                                p.getMethod() != null ? p.getMethod().name() : null,
                                                p.getInvoice().getInvoiceNumber()))
                                .collect(Collectors.toList());

                List<RevenueByClientDto> topClientsByRevenue = invoiceRepository.findTopClientsByRevenue(tenantId,
                                PageRequest.of(0, 5));

                long approvedTimesheetsReadyForInvoicing = timesheetRepository
                                .findApprovedTimesheetsForMonth(tenantId, now.getMonthValue(), now.getYear()).size();

                LocalDate sixMonthsAgo = now.minusMonths(6).withDayOfMonth(1);
                List<MonthlyRevenueDto> monthlyRevenueTrend = invoiceRepository.findMonthlyRevenueTrend(sixMonthsAgo,
                                tenantId);

                return new FinancialDashboardDto(
                                totalRevenueThisMonth,
                                totalRevenueThisYear,
                                outstandingAmount,
                                averageInvoiceAmount,
                                totalInvoices,
                                invoicesByStatus,
                                overdueInvoicesCount,
                                overdueAmount,
                                recentPayments,
                                topClientsByRevenue,
                                approvedTimesheetsReadyForInvoicing,
                                monthlyRevenueTrend);
        }
}
