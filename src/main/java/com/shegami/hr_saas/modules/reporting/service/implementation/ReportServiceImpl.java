package com.shegami.hr_saas.modules.reporting.service.implementation;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.billing.entity.Invoice;
import com.shegami.hr_saas.modules.billing.enums.InvoiceStatus;
import com.shegami.hr_saas.modules.billing.repository.InvoiceRepository;
import com.shegami.hr_saas.modules.billing.repository.PaymentRepository;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.mission.entity.Mission;
import com.shegami.hr_saas.modules.mission.enums.ConsultantStatus;
import com.shegami.hr_saas.modules.mission.enums.MissionStatus;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.mission.repository.MissionRepository;
import com.shegami.hr_saas.modules.reporting.dto.reports.*;
import com.shegami.hr_saas.modules.reporting.dto.shared.*;
import com.shegami.hr_saas.modules.reporting.service.ReportService;
import com.shegami.hr_saas.modules.timesheet.enums.TimesheetStatus;
import com.shegami.hr_saas.modules.timesheet.repository.TimesheetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

        private final ConsultantRepository consultantRepository;
        private final MissionRepository missionRepository;
        private final TimesheetRepository timesheetRepository;
        private final InvoiceRepository invoiceRepository;
        private final PaymentRepository paymentRepository;

        @Override
        public WorkforceReportDto getWorkforceReport(LocalDate startDate, LocalDate endDate) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("Generating workforce report for tenant {}", tenantId);

                long totalConsultants = consultantRepository.countByTenantTenantId(tenantId);

                List<CountByStatusDto> consultantsByStatus = consultantRepository.countByStatusAndTenantId(tenantId);
                List<CountByStatusDto> consultantsBySeniority = consultantRepository
                                .countBySeniorityAndTenantId(tenantId);
                List<CountByStatusDto> consultantsByType = consultantRepository.countByTypeAndTenantId(tenantId);

                long benchedCount = consultantsByStatus.stream()
                                .filter(c -> ConsultantStatus.AVAILABLE.name().equals(c.label()))
                                .mapToLong(CountByStatusDto::count)
                                .sum();

                long onMissionCount = consultantsByStatus.stream()
                                .filter(c -> ConsultantStatus.ON_MISSION.name().equals(c.label()))
                                .mapToLong(CountByStatusDto::count)
                                .sum();

                double utilizationRate = totalConsultants > 0
                                ? (double) onMissionCount / totalConsultants * 100
                                : 0.0;

                BigDecimal avgDailyCost = consultantRepository.averageInternalDailyCostByTenantId(tenantId);
                if (avgDailyCost == null)
                        avgDailyCost = BigDecimal.ZERO;

                // Monthly utilization — compute from data for last 12 months
                List<MonthlyUtilizationDto> monthlyUtilization = computeMonthlyUtilization(tenantId);

                // Top skills
                List<SkillCountDto> topSkills = computeTopSkills(tenantId);

                return new WorkforceReportDto(
                                Math.round(utilizationRate * 100.0) / 100.0,
                                totalConsultants,
                                benchedCount,
                                avgDailyCost.setScale(2, RoundingMode.HALF_UP),
                                monthlyUtilization,
                                consultantsByStatus,
                                consultantsBySeniority,
                                consultantsByType,
                                topSkills);
        }

        @Override
        public RevenueReportDto getRevenueReport(LocalDate startDate, LocalDate endDate) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("Generating revenue report for tenant {}", tenantId);

                LocalDate now = LocalDate.now();
                LocalDate effectiveStart = startDate != null ? startDate : now.minusMonths(12).withDayOfMonth(1);
                LocalDate effectiveEnd = endDate != null ? endDate : now;

                // Current period revenue (paid invoices)
                BigDecimal totalRevenue = invoiceRepository.sumTotalAmountByStatusInAndIssueDateBetweenAndTenantId(
                                List.of(InvoiceStatus.PAID), effectiveStart, effectiveEnd, tenantId);
                if (totalRevenue == null)
                        totalRevenue = BigDecimal.ZERO;

                // Previous period revenue (same duration, shifted back)
                long daysDiff = ChronoUnit.DAYS.between(effectiveStart, effectiveEnd);
                LocalDate prevStart = effectiveStart.minusDays(daysDiff);
                LocalDate prevEnd = effectiveStart.minusDays(1);
                BigDecimal lastPeriodRevenue = invoiceRepository.sumTotalAmountByStatusInAndIssueDateBetweenAndTenantId(
                                List.of(InvoiceStatus.PAID), prevStart, prevEnd, tenantId);
                if (lastPeriodRevenue == null)
                        lastPeriodRevenue = BigDecimal.ZERO;

                double growthPct = lastPeriodRevenue.compareTo(BigDecimal.ZERO) > 0
                                ? totalRevenue.subtract(lastPeriodRevenue)
                                                .divide(lastPeriodRevenue, 4, RoundingMode.HALF_UP)
                                                .doubleValue() * 100
                                : 0.0;

                // Average daily rate (total revenue / total days worked is approximated by avg
                // invoice amount)
                BigDecimal avgDailyRate = invoiceRepository.averagePaidInvoiceAmountByTenantId(tenantId);
                if (avgDailyRate == null)
                        avgDailyRate = BigDecimal.ZERO;

                // Monthly revenue trend
                List<MonthlyRevenueDto> monthlyRevenue = invoiceRepository.findMonthlyRevenueTrend(effectiveStart,
                                tenantId);

                // Revenue by client (top 10)
                List<RevenueByClientDto> revenueByClient = invoiceRepository.findTopClientsByRevenue(tenantId,
                                PageRequest.of(0, 10));

                // Revenue by project (top 10)
                List<RevenueByProjectDto> revenueByProject = invoiceRepository.findRevenueByProject(tenantId,
                                PageRequest.of(0, 10));

                return new RevenueReportDto(
                                totalRevenue,
                                lastPeriodRevenue,
                                Math.round(growthPct * 100.0) / 100.0,
                                avgDailyRate.setScale(2, RoundingMode.HALF_UP),
                                monthlyRevenue,
                                revenueByClient,
                                revenueByProject);
        }

        @Override
        public MissionReportDto getMissionReport(LocalDate startDate, LocalDate endDate) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("Generating mission report for tenant {}", tenantId);

                LocalDate now = LocalDate.now();
                LocalDate effectiveStart = startDate != null ? startDate : now.minusMonths(12).withDayOfMonth(1);

                // Totals
                List<CountByStatusDto> missionsByStatus = missionRepository.countByStatusAndTenantId(tenantId);
                long totalMissions = missionsByStatus.stream().mapToLong(CountByStatusDto::count).sum();

                long activeMissions = missionRepository.countByTenantTenantIdAndStatusIn(tenantId,
                                List.of(MissionStatus.ACTIVE, MissionStatus.IN_PROGRESS));

                long completedMissions = missionRepository.countByTenantTenantIdAndStatus(tenantId,
                                MissionStatus.COMPLETED)
                                + missionRepository.countByTenantTenantIdAndStatus(tenantId, MissionStatus.CLOSED);

                double completionRate = totalMissions > 0 ? (double) completedMissions / totalMissions * 100 : 0.0;

                // Average mission duration
                double avgDuration = computeAverageMissionDuration(tenantId);

                // Priority breakdown
                List<CountByStatusDto> missionsByPriority = missionRepository.countByPriorityAndTenantId(tenantId);

                // Missions started per month
                List<MonthlyCountDto> missionsStartedByMonth = missionRepository.countStartedByMonthAndTenantId(
                                effectiveStart, tenantId);

                // Clients with most missions (top 10)
                List<ClientMissionCountDto> clientsWithMostMissions = missionRepository.countByClientAndTenantId(
                                tenantId, PageRequest.of(0, 10));

                return new MissionReportDto(
                                totalMissions,
                                activeMissions,
                                completedMissions,
                                Math.round(avgDuration * 100.0) / 100.0,
                                Math.round(completionRate * 100.0) / 100.0,
                                missionsByStatus,
                                missionsByPriority,
                                missionsStartedByMonth,
                                clientsWithMostMissions);
        }

        @Override
        public TimesheetReportDto getTimesheetReport(LocalDate startDate, LocalDate endDate) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("Generating timesheet report for tenant {}", tenantId);

                long totalTimesheets = timesheetRepository.countByTenantTenantId(tenantId);

                List<CountByStatusDto> timesheetsByStatus = timesheetRepository.countByStatusAndTenantId(tenantId);

                long submitted = timesheetRepository.countByTenantTenantIdAndStatus(tenantId,
                                TimesheetStatus.SUBMITTED);
                long approved = timesheetRepository.countByTenantTenantIdAndStatus(tenantId, TimesheetStatus.APPROVED);
                long rejected = timesheetRepository.countByTenantTenantIdAndStatus(tenantId, TimesheetStatus.REJECTED);
                long invoiced = timesheetRepository.countByTenantTenantIdAndStatus(tenantId, TimesheetStatus.INVOICED);

                long totalProcessed = submitted + approved + rejected + invoiced;
                double submissionRate = totalTimesheets > 0
                                ? (double) totalProcessed / totalTimesheets * 100
                                : 0.0;
                double approvalRate = totalProcessed > 0
                                ? (double) (approved + invoiced) / totalProcessed * 100
                                : 0.0;
                double rejectionRate = totalProcessed > 0
                                ? (double) rejected / totalProcessed * 100
                                : 0.0;

                // Average days worked per month (approx: total approved quantity / number of
                // distinct months)
                double avgDaysPerMonth = 0.0;
                // Simplified: use total approved quantity / 12 as rough average
                // In production, you might query sumApprovedQuantity / distinct months

                // Monthly submission trend
                List<MonthlyCountDto> monthlySubmissions = timesheetRepository
                                .countSubmissionsByMonthAndTenantId(tenantId);

                // Top consultants by total hours (top 10)
                List<ConsultantHoursDto> topConsultantsByHours = timesheetRepository.findTopConsultantsByHours(
                                tenantId, PageRequest.of(0, 10));

                return new TimesheetReportDto(
                                totalTimesheets,
                                Math.round(submissionRate * 100.0) / 100.0,
                                Math.round(approvalRate * 100.0) / 100.0,
                                Math.round(rejectionRate * 100.0) / 100.0,
                                avgDaysPerMonth,
                                timesheetsByStatus,
                                monthlySubmissions,
                                topConsultantsByHours);
        }

        @Override
        public BillingReportDto getBillingReport(LocalDate startDate, LocalDate endDate) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("Generating billing report for tenant {}", tenantId);

                LocalDate now = LocalDate.now();
                LocalDate effectiveStart = startDate != null ? startDate : now.minusMonths(12).withDayOfMonth(1);

                // Total amounts
                BigDecimal totalInvoiced = invoiceRepository.sumTotalInvoicedByTenantId(tenantId);
                if (totalInvoiced == null)
                        totalInvoiced = BigDecimal.ZERO;

                BigDecimal totalPaid = invoiceRepository.sumPaidAmountByTenantId(tenantId);
                if (totalPaid == null)
                        totalPaid = BigDecimal.ZERO;

                BigDecimal totalOutstanding = invoiceRepository.sumTotalAmountByStatusInAndTenantId(
                                List.of(InvoiceStatus.SENT, InvoiceStatus.OVERDUE, InvoiceStatus.PDF_PENDING),
                                tenantId);
                if (totalOutstanding == null)
                        totalOutstanding = BigDecimal.ZERO;

                BigDecimal totalOverdue = invoiceRepository.sumTotalAmountByStatusInAndTenantId(
                                List.of(InvoiceStatus.OVERDUE), tenantId);
                if (totalOverdue == null)
                        totalOverdue = BigDecimal.ZERO;

                // Collection rate
                double collectionRate = totalInvoiced.compareTo(BigDecimal.ZERO) > 0
                                ? totalPaid.divide(totalInvoiced, 4, RoundingMode.HALF_UP).doubleValue() * 100
                                : 0.0;

                // DSO (Days Sales Outstanding) — average days from issue to payment for
                // outstanding invoices
                double averageDSO = computeAverageDSO(tenantId);

                // Invoices by status
                List<CountByStatusDto> invoicesByStatus = invoiceRepository.countByStatusAndTenantId(tenantId);

                // Monthly invoiced amounts
                List<MonthlyRevenueDto> monthlyInvoiced = invoiceRepository.findMonthlyInvoicedTrend(effectiveStart,
                                tenantId);

                // Invoice aging buckets
                List<AgingBucketDto> agingBuckets = computeInvoiceAgingBuckets(tenantId);

                // Payments by method
                List<CountByStatusDto> paymentsByMethod = paymentRepository.countByMethodAndTenantId(tenantId);

                return new BillingReportDto(
                                totalInvoiced,
                                totalPaid,
                                totalOutstanding,
                                totalOverdue,
                                Math.round(collectionRate * 100.0) / 100.0,
                                Math.round(averageDSO * 100.0) / 100.0,
                                invoicesByStatus,
                                monthlyInvoiced,
                                agingBuckets,
                                paymentsByMethod);
        }

        /**
         * Compute monthly utilization rate for the last 12 months.
         * For each month, checks how many consultants had active missions vs total.
         */
        private List<MonthlyUtilizationDto> computeMonthlyUtilization(String tenantId) {
                List<MonthlyUtilizationDto> result = new ArrayList<>();
                LocalDate now = LocalDate.now();
                long totalConsultants = consultantRepository.countByTenantTenantId(tenantId);
                if (totalConsultants == 0)
                        return result;

                List<Mission> allMissions = missionRepository.findAllByTenantId(tenantId);

                for (int i = 11; i >= 0; i--) {
                        LocalDate monthDate = now.minusMonths(i);
                        int month = monthDate.getMonthValue();
                        int year = monthDate.getYear();

                        LocalDate monthStart = LocalDate.of(year, month, 1);
                        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

                        // Count missions that overlap with this month
                        long activeInMonth = allMissions.stream()
                                        .filter(m -> m.getStartDate() != null && m.getEndDate() != null)
                                        .filter(m -> !m.getStartDate().isAfter(monthEnd)
                                                        && !m.getEndDate().isBefore(monthStart))
                                        .flatMap(m -> m.getConsultants() != null ? m.getConsultants().stream()
                                                        : java.util.stream.Stream.empty())
                                        .distinct()
                                        .count();

                        double rate = (double) activeInMonth / totalConsultants * 100;
                        result.add(new MonthlyUtilizationDto(month, year, Math.round(rate * 100.0) / 100.0));
                }

                return result;
        }

        /**
         * Compute top skills across all consultants.
         */
        private List<SkillCountDto> computeTopSkills(String tenantId) {
                List<Consultant> consultants = consultantRepository.findAllByTenantId(tenantId);

                Map<String, Long> skillCounts = consultants.stream()
                                .filter(c -> c.getSkills() != null)
                                .flatMap(c -> c.getSkills().stream())
                                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

                return skillCounts.entrySet().stream()
                                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                .limit(15)
                                .map(e -> new SkillCountDto(e.getKey(), e.getValue()))
                                .collect(Collectors.toList());
        }

        /**
         * Compute average mission duration in days for completed/closed missions.
         */
        private double computeAverageMissionDuration(String tenantId) {
                List<Mission> allMissions = missionRepository.findAllByTenantId(tenantId);

                List<Long> durations = allMissions.stream()
                                .filter(m -> m.getStartDate() != null && m.getEndDate() != null)
                                .filter(m -> m.getStatus() == MissionStatus.COMPLETED
                                                || m.getStatus() == MissionStatus.CLOSED)
                                .map(m -> ChronoUnit.DAYS.between(m.getStartDate(), m.getEndDate()))
                                .collect(Collectors.toList());

                if (durations.isEmpty())
                        return 0.0;
                return durations.stream().mapToLong(Long::longValue).average().orElse(0.0);
        }

        /**
         * Compute average Days Sales Outstanding for outstanding invoices.
         */
        private double computeAverageDSO(String tenantId) {
                List<Invoice> outstanding = invoiceRepository.findAllSentAndOverdueByTenantId(tenantId);
                if (outstanding.isEmpty())
                        return 0.0;

                LocalDate now = LocalDate.now();
                double totalDays = outstanding.stream()
                                .filter(i -> i.getIssueDate() != null)
                                .mapToLong(i -> ChronoUnit.DAYS.between(i.getIssueDate(), now))
                                .average()
                                .orElse(0.0);

                return totalDays;
        }

        /**
         * Compute invoice aging buckets: 0-30 days, 31-60 days, 61-90 days, 90+ days.
         */
        private List<AgingBucketDto> computeInvoiceAgingBuckets(String tenantId) {
                List<Invoice> outstanding = invoiceRepository.findAllSentAndOverdueByTenantId(tenantId);
                LocalDate now = LocalDate.now();

                long count0to30 = 0, count31to60 = 0, count61to90 = 0, count90plus = 0;
                BigDecimal amount0to30 = BigDecimal.ZERO, amount31to60 = BigDecimal.ZERO,
                                amount61to90 = BigDecimal.ZERO, amount90plus = BigDecimal.ZERO;

                for (Invoice inv : outstanding) {
                        if (inv.getIssueDate() == null)
                                continue;
                        long daysOld = ChronoUnit.DAYS.between(inv.getIssueDate(), now);
                        BigDecimal amt = inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO;

                        if (daysOld <= 30) {
                                count0to30++;
                                amount0to30 = amount0to30.add(amt);
                        } else if (daysOld <= 60) {
                                count31to60++;
                                amount31to60 = amount31to60.add(amt);
                        } else if (daysOld <= 90) {
                                count61to90++;
                                amount61to90 = amount61to90.add(amt);
                        } else {
                                count90plus++;
                                amount90plus = amount90plus.add(amt);
                        }
                }

                return List.of(
                                new AgingBucketDto("0-30 days", count0to30, amount0to30),
                                new AgingBucketDto("31-60 days", count31to60, amount31to60),
                                new AgingBucketDto("61-90 days", count61to90, amount61to90),
                                new AgingBucketDto("90+ days", count90plus, amount90plus));
        }
}
