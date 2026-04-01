package com.shegami.hr_saas.modules.timesheet.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.config.domain.rabbitMq.RabbitMQConfig;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.billing.entity.Invoice;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.hr.exception.EmployeeNotFoundException;
import com.shegami.hr_saas.modules.hr.repository.EmployeeRepository;
import com.shegami.hr_saas.modules.mission.entity.Mission;
import com.shegami.hr_saas.modules.mission.exceptions.MissionNotFoundException;
import com.shegami.hr_saas.modules.mission.repository.MissionRepository;
import com.shegami.hr_saas.modules.timesheet.dto.*;
import com.shegami.hr_saas.modules.timesheet.entity.Timesheet;
import com.shegami.hr_saas.modules.timesheet.entity.TimesheetEntry;
import com.shegami.hr_saas.modules.timesheet.enums.TimesheetStatus;
import com.shegami.hr_saas.modules.timesheet.exceptions.*;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.mission.exceptions.ConsultantNotFoundException;
import com.shegami.hr_saas.modules.notifications.dto.NotificationMessage;
import com.shegami.hr_saas.modules.notifications.enums.EntityType;
import com.shegami.hr_saas.modules.notifications.enums.NotificationType;
import com.shegami.hr_saas.modules.timesheet.mapper.TimesheetMapper;
import com.shegami.hr_saas.modules.timesheet.repository.TimesheetRepository;
import com.shegami.hr_saas.modules.timesheet.service.TimesheetService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class TimesheetServiceImpl implements TimesheetService {

        private final TimesheetRepository timesheetRepository;
        private final MissionRepository missionRepository;
        @Qualifier("timesheetMapper")
        private final TimesheetMapper mapper;
        private final EmployeeRepository employeeRepository;
        private final TenantService tenantService;
        private final ConsultantRepository consultantRepository;
        private final RabbitTemplate rabbitTemplate;

        // -------------------------------------------------------------------------
        // Public API
        // -------------------------------------------------------------------------

        @Transactional
        @Override
        public TimesheetResponse createTimesheet(CreateTimesheetRequest req) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                String consultantId = UserContextHolder.getCurrentUserContext().userId();

                log.info("[Timesheet] Creating timesheet | tenantId={} missionId={} consultantId={} period={}/{}",
                                tenantId, req.missionId(), consultantId, req.year(), req.month());

                Consultant consultant = consultantRepository.findByUserUserId(consultantId)
                                .orElseThrow(() -> {
                                        log.warn("[Timesheet] Consultant not found | userId={}", consultantId);
                                        return new ConsultantNotFoundException(
                                                        "Consultant not found for user: " + consultantId);
                                });

                Tenant tenant = tenantService.getTenant(tenantId);

                Mission mission = missionRepository
                                .findByMissionIdAndTenantTenantId(req.missionId(), tenantId)
                                .orElseThrow(() -> {
                                        log.warn("[Timesheet] Mission not found | missionId={} tenantId={}",
                                                        req.missionId(), tenantId);
                                        return new MissionNotFoundException("Mission not found: " + req.missionId());
                                });

                boolean alreadyExists = timesheetRepository
                                .existsByMissionAndConsultantAndPeriod(req.missionId(), consultantId, req.month(),
                                                req.year());
                if (alreadyExists) {
                        log.warn("[Timesheet] Duplicate timesheet rejected | missionId={} consultantId={} period={}/{}",
                                        req.missionId(), consultantId, req.month(), req.year());
                        throw new DuplicateTimesheetException(
                                        "A timesheet already exists for this mission and period.");
                }

                Timesheet timesheet = new Timesheet();
                timesheet.setMission(mission);
                timesheet.setMonth(req.month());
                timesheet.setYear(req.year());
                timesheet.setStatus(TimesheetStatus.DRAFT);
                timesheet.setTenant(tenant);
                timesheet.setConsultant(consultant);

                Timesheet saved = timesheetRepository.save(timesheet);
                log.info("[Timesheet] Timesheet created | timesheetId={}", saved.getTimesheetId());

                return mapper.toResponse(saved);
        }

        @Transactional
        @Override
        public TimesheetResponse saveEntries(String timesheetId, SaveEntriesRequest req) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("[Timesheet] Saving entries | timesheetId={} tenantId={} entryCount={}",
                                timesheetId, tenantId, req.entries().size());

                Timesheet timesheet = resolveEditableTimesheet(timesheetId, tenantId);
                YearMonth period = YearMonth.of(timesheet.getYear(), timesheet.getMonth());

                req.entries().forEach(entry -> {
                        validateDateInPeriod(entry.date(), period);
                        validateDateInMissionRange(entry.date(), timesheet.getMission());
                        validateQuantity(entry.quantity());
                });

                timesheet.getEntries().clear();
                req.entries().forEach(e -> {
                        TimesheetEntry entry = new TimesheetEntry();
                        entry.setTimesheet(timesheet);
                        entry.setDate(e.date());
                        entry.setQuantity(e.quantity());
                        entry.setComment(e.comment());
                        timesheet.getEntries().add(entry);
                });

                Timesheet saved = timesheetRepository.save(timesheet);
                log.info("[Timesheet] Entries saved | timesheetId={} entryCount={}", timesheetId, req.entries().size());

                return mapper.toResponse(saved);
        }

        @Transactional
        @Override
        public TimesheetResponse submitTimesheet(String timesheetId) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.info("[Timesheet] Submitting timesheet | timesheetId={} tenantId={}", timesheetId, tenantId);

                Timesheet timesheet = resolveEditableTimesheet(timesheetId, tenantId);

                if (timesheet.getEntries().isEmpty()) {
                        log.warn("[Timesheet] Submit rejected — no entries | timesheetId={}", timesheetId);
                        throw new EmptyTimesheetException("Cannot submit an empty timesheet.");
                }

                timesheet.setStatus(TimesheetStatus.SUBMITTED);
                Timesheet saved = timesheetRepository.save(timesheet);
                log.info("[Timesheet] Timesheet submitted | timesheetId={}", timesheetId);

                // Notify account manager after commit — notification failure must not roll back
                // submission
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                                sendTimesheetSubmittedNotification(saved);
                        }
                });

                return mapper.toResponse(saved);
        }

        @Transactional
        @Override
        public TimesheetResponse reviewTimesheet(String timesheetId, ReviewTimesheetRequest req) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                String userId = UserContextHolder.getCurrentUserContext().userId();

                log.info("[Timesheet] Reviewing timesheet | timesheetId={} reviewerId={} approved={}",
                                timesheetId, userId, req.approved());

                Employee reviewer = employeeRepository.findByUserUserId(userId)
                                .orElseThrow(() -> {
                                        log.warn("[Timesheet] Reviewer not found | userId={}", userId);
                                        return new EmployeeNotFoundException("Employee not found: " + userId);
                                });

                Timesheet timesheet = timesheetRepository
                                .findByIdAndTenant(timesheetId, tenantId)
                                .orElseThrow(() -> {
                                        log.warn("[Timesheet] Timesheet not found for review | timesheetId={} tenantId={}",
                                                        timesheetId, tenantId);
                                        return new TimesheetNotFoundException("Timesheet not found: " + timesheetId);
                                });

                if (timesheet.getStatus() != TimesheetStatus.SUBMITTED) {
                        log.warn("[Timesheet] Review rejected — wrong status | timesheetId={} status={}",
                                        timesheetId, timesheet.getStatus());
                        throw new IllegalTimesheetStateException(
                                        "Only SUBMITTED timesheets can be reviewed. Current status: "
                                                        + timesheet.getStatus());
                }

                if (!req.approved() && (req.comment() == null || req.comment().isBlank())) {
                        log.warn("[Timesheet] Rejection requires a comment | timesheetId={}", timesheetId);
                        throw new IllegalArgumentException("A comment is required when rejecting a timesheet.");
                }

                TimesheetStatus newStatus = req.approved() ? TimesheetStatus.APPROVED : TimesheetStatus.REJECTED;
                timesheet.setStatus(newStatus);
                timesheet.setValidatedAt(LocalDateTime.now());
                timesheet.setValidatedBy(reviewer);
                if (!req.approved()) {
                        timesheet.setRejectionComment(req.comment());
                }

                Timesheet saved = timesheetRepository.save(timesheet);
                log.info("[Timesheet] Review completed | timesheetId={} newStatus={} reviewerId={}",
                                timesheetId, saved.getStatus(), userId);

                // CRITICAL: Both the billing event and the notification are published AFTER
                // commit.
                //
                // Publishing inside the transaction caused a race condition where the billing
                // consumer could pick up the event and query the timesheet before this
                // transaction
                // committed, seeing stale or missing data. afterCommit() guarantees the
                // consumer
                // always reads the final, committed state.
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                                if (saved.getStatus() == TimesheetStatus.APPROVED) {
                                        publishTimesheetApprovedEvent(saved);
                                }
                                sendTimesheetReviewedNotification(saved, userId);
                        }
                });

                return mapper.toResponse(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public List<TimesheetResponse> getPendingTimesheets() {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                String userId = UserContextHolder.getCurrentUserContext().userId();
                log.debug("[Timesheet] Fetching pending timesheets | managerId={} tenantId={}", userId, tenantId);

                List<TimesheetResponse> pending = timesheetRepository
                                .findSubmittedTimesheetsForManager(userId, tenantId)
                                .stream()
                                .map(mapper::toResponse)
                                .toList();

                log.debug("[Timesheet] Pending timesheets resolved | managerId={} count={}", userId, pending.size());
                return pending;
        }

        @Override
        @Transactional(readOnly = true)
        public List<TimesheetResponse> getConsultantHistory(String consultantId) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.debug("[Timesheet] Fetching consultant history | consultantId={} tenantId={}", consultantId,
                                tenantId);

                List<TimesheetResponse> history = timesheetRepository
                                .findHistoryByConsultant(consultantId, tenantId)
                                .stream()
                                .map(mapper::toResponse)
                                .toList();

                log.debug("[Timesheet] History resolved | consultantId={} count={}", consultantId, history.size());
                return history;
        }

        @Override
        @Transactional(readOnly = true)
        public TimesheetResponse getTimesheet(String timesheetId) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.debug("[Timesheet] Fetching timesheet | timesheetId={} tenantId={}", timesheetId, tenantId);

                return timesheetRepository
                                .findByIdAndTenant(timesheetId, tenantId)
                                .map(mapper::toResponse)
                                .orElseThrow(() -> {
                                        log.warn("[Timesheet] Not found | timesheetId={} tenantId={}", timesheetId,
                                                        tenantId);
                                        return new TimesheetNotFoundException("Timesheet not found: " + timesheetId);
                                });
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TimesheetResponse> getAllTimesheets(Pageable pageable) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.debug("[Timesheet] Fetching all timesheets | tenantId={} page={} size={}",
                                tenantId, pageable.getPageNumber(), pageable.getPageSize());

                Page<TimesheetResponse> result = timesheetRepository
                                .findAllByTenantTenantId(tenantId, pageable)
                                .map(mapper::toResponse);

                log.debug("[Timesheet] All timesheets resolved | tenantId={} total={}", tenantId,
                                result.getTotalElements());
                return result;
        }

        @Override
        @Transactional(readOnly = true)
        public List<TimesheetResponse> getApprovedTimesheetsByClient(String clientId) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                log.debug("[Timesheet] Fetching approved timesheets by client | clientId={} tenantId={}", clientId,
                                tenantId);

                return timesheetRepository.findApprovedByClient(clientId, tenantId)
                                .stream()
                                .map(mapper::toResponse)
                                .toList();
        }

        // -------------------------------------------------------------------------
        // Internal — Event Publishing
        // -------------------------------------------------------------------------

        /**
         * Published only in afterCommit() to eliminate the race condition where
         * the billing consumer could read the timesheet before this transaction
         * commits.
         */
        private void publishTimesheetApprovedEvent(Timesheet timesheet) {
                log.info("[Timesheet] Publishing TimesheetApprovedEvent | timesheetId={}", timesheet.getTimesheetId());
                TimesheetApprovedEvent event = new TimesheetApprovedEvent(
                                timesheet.getTimesheetId(),
                                timesheet.getTenant().getTenantId(),
                                timesheet.getMission().getMissionId());
                rabbitTemplate.convertAndSend(RabbitMQConfig.BILLING_EXCHANGE, "billing.timesheet.approved", event);
                log.info("[Timesheet] TimesheetApprovedEvent published | timesheetId={}", timesheet.getTimesheetId());
        }

        // -------------------------------------------------------------------------
        // Internal — Notification Helpers
        // -------------------------------------------------------------------------

        private void sendTimesheetSubmittedNotification(Timesheet timesheet) {
                Employee accountManager = timesheet.getMission().getAccountManager();
                if (accountManager == null || accountManager.getUser() == null) {
                        log.warn("[Timesheet] Skipping submission notification — no account manager | timesheetId={}",
                                        timesheet.getTimesheetId());
                        return;
                }

                String consultantName = timesheet.getConsultant() != null
                                ? timesheet.getConsultant().getFirstName()
                                : "A consultant";

                NotificationMessage msg = NotificationMessage.builder()
                                .userId(accountManager.getUser().getUserId())
                                .notificationType(NotificationType.TIMESHEET_SUBMITTED)
                                .title(NotificationType.TIMESHEET_SUBMITTED.getDefaultTitle())
                                .message(String.format("%s submitted a timesheet for mission '%s'.",
                                                consultantName, timesheet.getMission().getTitle()))
                                .entityType(EntityType.TIMESHEET)
                                .entityId(timesheet.getTimesheetId())
                                .actorId(UserContextHolder.getCurrentUserContext().userId())
                                .metadata(Map.of(
                                                "timesheetId", timesheet.getTimesheetId(),
                                                "missionTitle", timesheet.getMission().getTitle(),
                                                "missionId", timesheet.getMission().getMissionId(),
                                                "consultantName", consultantName,
                                                "month", timesheet.getMonth(),
                                                "year", timesheet.getYear(),
                                                "totalDays",
                                                timesheet.getEntries().stream().mapToDouble(TimesheetEntry::getQuantity)
                                                                .sum()))
                                .build();

                rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                "notification.timesheet.submitted", msg);

                log.info("[Timesheet] Submission notification sent | timesheetId={} recipientUserId={}",
                                timesheet.getTimesheetId(), msg.getUserId());
        }

        private void sendTimesheetReviewedNotification(Timesheet timesheet, String reviewerUserId) {
                if (timesheet.getConsultant() == null || timesheet.getConsultant().getUser() == null) {
                        log.warn("[Timesheet] Skipping review notification — no consultant user | timesheetId={}",
                                        timesheet.getTimesheetId());
                        return;
                }

                boolean approved = timesheet.getStatus() == TimesheetStatus.APPROVED;
                NotificationType notificationType = approved
                                ? NotificationType.TIMESHEET_APPROVED
                                : NotificationType.TIMESHEET_REJECTED;

                NotificationMessage msg = NotificationMessage.builder()
                                .userId(timesheet.getConsultant().getUser().getUserId())
                                .notificationType(notificationType)
                                .title(notificationType.getDefaultTitle())
                                .message(String.format("Your timesheet for mission '%s' was %s.",
                                                timesheet.getMission().getTitle(), approved ? "approved" : "rejected"))
                                .entityType(EntityType.TIMESHEET)
                                .entityId(timesheet.getTimesheetId())
                                .actorId(reviewerUserId)
                                .metadata(Map.of(
                                                "timesheetId", timesheet.getTimesheetId(),
                                                "missionTitle", timesheet.getMission().getTitle(),
                                                "missionId", timesheet.getMission().getMissionId(),
                                                "month", timesheet.getMonth(),
                                                "year", timesheet.getYear(),
                                                "totalDays",
                                                timesheet.getEntries().stream().mapToDouble(TimesheetEntry::getQuantity)
                                                                .sum(),
                                                "rejectionComment",
                                                timesheet.getStatus() == TimesheetStatus.REJECTED
                                                                ? timesheet.getRejectionComment() != null
                                                                                ? timesheet.getRejectionComment()
                                                                                : "N/A"
                                                                : "N/A",
                                                "validatedBy",
                                                timesheet.getValidatedBy() != null
                                                                ? timesheet.getValidatedBy().getUser().getFullName()
                                                                : "System"))
                                .build();

                rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                "notification.timesheet.reviewed", msg);

                log.info("[Timesheet] Review notification sent | timesheetId={} status={} recipientUserId={}",
                                timesheet.getTimesheetId(), timesheet.getStatus(), msg.getUserId());
        }

        // -------------------------------------------------------------------------
        // Internal — Validation & Guards
        // -------------------------------------------------------------------------

        /**
         * Timesheets in SUBMITTED or APPROVED state are locked — they cannot be edited.
         * DRAFT and REJECTED timesheets are open for modification.
         */
        private Timesheet resolveEditableTimesheet(String timesheetId, String tenantId) {
                Timesheet timesheet = timesheetRepository
                                .findByIdAndTenant(timesheetId, tenantId)
                                .orElseThrow(() -> {
                                        log.warn("[Timesheet] Timesheet not found | timesheetId={} tenantId={}",
                                                        timesheetId, tenantId);
                                        return new TimesheetNotFoundException("Timesheet not found: " + timesheetId);
                                });

                if (timesheet.getStatus() == TimesheetStatus.SUBMITTED
                                || timesheet.getStatus() == TimesheetStatus.APPROVED) {
                        log.warn("[Timesheet] Edit rejected — timesheet is locked | timesheetId={} status={}",
                                        timesheetId, timesheet.getStatus());
                        throw new TimesheetModificationForbiddenException(
                                        "Timesheet is locked and cannot be edited. Status: " + timesheet.getStatus());
                }

                return timesheet;
        }

        private void validateDateInPeriod(LocalDate date, YearMonth period) {
                if (!YearMonth.from(date).equals(period)) {
                        throw new IllegalArgumentException(
                                        "Date " + date + " does not belong to period " + period);
                }
        }

        private void validateDateInMissionRange(LocalDate date, Mission mission) {
                if (mission.getStartDate() != null && date.isBefore(mission.getStartDate())) {
                        throw new IllegalArgumentException(
                                        "Date " + date + " is before mission start date " + mission.getStartDate());
                }
                if (mission.getEndDate() != null && date.isAfter(mission.getEndDate())) {
                        throw new IllegalArgumentException(
                                        "Date " + date + " is after mission end date " + mission.getEndDate());
                }
        }

        private void validateQuantity(Double quantity) {
                if (quantity != 0.0 && quantity != 0.5 && quantity != 1.0) {
                        throw new IllegalArgumentException(
                                        "Quantity must be 0.0 (off), 0.5 (half-day), or 1.0 (full day). Got: "
                                                        + quantity);
                }
        }
}