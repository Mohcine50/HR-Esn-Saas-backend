package com.shegami.hr_saas.modules.timesheet.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.service.TenantService;
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
import com.shegami.hr_saas.modules.timesheet.exceptions.TimesheetNotFoundException;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.mission.exceptions.ConsultantNotFoundException;
import com.shegami.hr_saas.modules.notifications.dto.NotificationMessage;
import com.shegami.hr_saas.modules.timesheet.mapper.TimesheetMapper;
import com.shegami.hr_saas.modules.timesheet.repository.TimesheetRepository;
import com.shegami.hr_saas.modules.timesheet.service.TimesheetService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.shegami.hr_saas.config.domain.rabbitMq.RabbitMQConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;


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

    @Transactional
    @Override
    public TimesheetResponse createTimesheet(CreateTimesheetRequest req) {

        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        String userId = UserContextHolder.getCurrentUserContext().userId();

        Consultant consultant = consultantRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ConsultantNotFoundException("Consultant not found for user: " + userId));
        String consultantId = consultant.getConsultantId();

        log.info("[Timesheet] Creating timesheet | tenantId={} missionId={} consultantId={} period={}/{}",
                tenantId, req.missionId(), consultantId, req.month(), req.year());

        Tenant tenant = tenantService.getTenant(tenantId);

        Mission mission = missionRepository
                .findByMissionIdAndTenantTenantId(req.missionId(), tenantId)
                .orElseThrow(() -> {
                    log.warn("[Timesheet] Mission not found | missionId={} tenantId={}", req.missionId(), tenantId);
                    return new MissionNotFoundException("Mission not found: " + req.missionId());
                });

        boolean exists = timesheetRepository
                .existsByMissionAndConsultantAndPeriod(
                        req.missionId(), consultantId, req.month(), req.year());
        if (exists) {
            log.warn("[Timesheet] Duplicate timesheet rejected | missionId={} consultantId={} period={}/{}",
                    req.missionId(), consultantId, req.month(), req.year());
            throw new IllegalStateException("A timesheet already exists for this period.");
        }

        Timesheet timesheet = new Timesheet();
        timesheet.setMission(mission);
        timesheet.setMonth(req.month());
        timesheet.setYear(req.year());
        timesheet.setStatus(TimesheetStatus.DRAFT);
        timesheet.setTenant(tenant);
        timesheet.setConsultant(consultant);

        Timesheet saved = timesheetRepository.save(timesheet);
        log.info("[Timesheet] Timesheet created | timesheetId={} missionId={} consultantId={} period={}/{}",
                saved.getTimesheetId(), req.missionId(), consultantId, req.month(), req.year());

        return mapper.toResponse(saved);
    }


    @Transactional
    @Override
    public TimesheetResponse saveEntries(String timesheetId, SaveEntriesRequest req) {

        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        log.info("[Timesheet] Saving entries | timesheetId={} tenantId={} entryCount={}",
                timesheetId, tenantId, req.entries().size());

        Tenant tenant = tenantService.getTenant(tenantId);

        Timesheet timesheet = getEditableTimesheet(timesheetId, tenantId);

        YearMonth period = YearMonth.of(timesheet.getYear(), timesheet.getMonth());
        for (UpsertEntryRequest entry : req.entries()) {
            validateDateInPeriod(entry.date(), period);
            validateDateInMissionRange(entry.date(), timesheet.getMission());
            validateQuantity(entry.quantity());
        }

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

        Tenant tenant = tenantService.getTenant(tenantId);

        Timesheet timesheet = getEditableTimesheet(timesheetId, tenantId);

        if (timesheet.getEntries().isEmpty()) {
            log.warn("[Timesheet] Submit rejected — no entries | timesheetId={}", timesheetId);
            throw new IllegalStateException("Cannot submit an empty timesheet.");
        }

        timesheet.setStatus(TimesheetStatus.SUBMITTED);
        Timesheet saved = timesheetRepository.save(timesheet);
        log.info("[Timesheet] Timesheet submitted | timesheetId={}", timesheetId);

        if (saved.getMission().getAccountManager() != null && saved.getMission().getAccountManager().getUser() != null) {
            String managerUserId = saved.getMission().getAccountManager().getUser().getUserId();
            NotificationMessage msg = NotificationMessage.builder()
                    .userId(managerUserId)
                    .notificationType("TIMESHEET_SUBMITTED")
                    .title("Timesheet Submitted for Review")
                    .message("A timesheet was submitted by " + (saved.getConsultant() != null ? saved.getConsultant().getFirstName() : "a consultant") + " for mission '" + saved.getMission().getTitle() + "'.")
                    .entityType("TIMESHEET")
                    .entityId(saved.getTimesheetId())
                    .actorId(UserContextHolder.getCurrentUserContext().userId())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, "notification.timesheet.submitted", msg);
        }

        return mapper.toResponse(saved);
    }


    @Transactional
    @Override
    public TimesheetResponse reviewTimesheet(
            String timesheetId,
            ReviewTimesheetRequest req) {

        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        String userId = UserContextHolder.getCurrentUserContext().userId();
        log.info("[Timesheet] Reviewing timesheet | timesheetId={} reviewerId={} approved={}",
                timesheetId, userId, req.approved());

        Employee manager = employeeRepository.findByUserUserId(userId)
                .orElseThrow(() -> {
                    log.warn("[Timesheet] Reviewer not found | userId={}", userId);
                    return new EmployeeNotFoundException("Employee not found: " + userId);
                });

        Timesheet timesheet = timesheetRepository
                .findByIdAndTenant(timesheetId, tenantId)
                .orElseThrow(() -> {
                    log.warn("[Timesheet] Timesheet not found for review | timesheetId={} tenantId={}", timesheetId, tenantId);
                    return new TimesheetNotFoundException("Timesheet not found.");
                });

        if (timesheet.getStatus() != TimesheetStatus.SUBMITTED) {
            log.warn("[Timesheet] Review rejected — wrong status | timesheetId={} status={}", timesheetId, timesheet.getStatus());
            throw new IllegalStateException("Only SUBMITTED timesheets can be reviewed.");
        }

        if (!req.approved() && (req.comment() == null || req.comment().isBlank())) {
            log.warn("[Timesheet] Rejection requires a comment | timesheetId={}", timesheetId);
            throw new IllegalArgumentException("A comment is required when rejecting a timesheet.");
        }

        timesheet.setStatus(req.approved() ? TimesheetStatus.APPROVED : TimesheetStatus.REJECTED);
        timesheet.setValidatedAt(LocalDateTime.now());
        timesheet.setValidatedBy(manager);

        // On rejection: reset to DRAFT so consultant can correct and resubmit
        if (!req.approved()) {
            timesheet.setStatus(TimesheetStatus.REJECTED);
        }

        Timesheet saved = timesheetRepository.save(timesheet);
        log.info("[Timesheet] Timesheet review completed | timesheetId={} newStatus={} reviewerId={}",
                timesheetId, saved.getStatus(), userId);

        if (saved.getStatus() == TimesheetStatus.APPROVED) {
            TimesheetApprovedEvent event = new TimesheetApprovedEvent(
                    saved.getTimesheetId(),
                    saved.getTenant().getTenantId(),
                    saved.getMission().getMissionId()
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.BILLING_EXCHANGE, "billing.timesheet.approved", event);
            log.info("[Timesheet] Published TimesheetApprovedEvent | timesheetId={}", saved.getTimesheetId());
        }

        if (saved.getConsultant() != null && saved.getConsultant().getUser() != null) {
            String consultantUserId = saved.getConsultant().getUser().getUserId();
            String statusVerb = saved.getStatus() == TimesheetStatus.APPROVED ? "approved" : "rejected";
            NotificationMessage msg = NotificationMessage.builder()
                    .userId(consultantUserId)
                    .notificationType("TIMESHEET_REVIEWED")
                    .title("Timesheet " + (saved.getStatus() == TimesheetStatus.APPROVED ? "Approved" : "Rejected"))
                    .message("Your timesheet for mission '" + saved.getMission().getTitle() + "' was \b" + statusVerb + "\b.")
                    .entityType("TIMESHEET")
                    .entityId(saved.getTimesheetId())
                    .actorId(userId)
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, "notification.timesheet.reviewed", msg);
        }

        return mapper.toResponse(saved);
    }

    @Override
    public List<TimesheetResponse> getPendingTimesheets() {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        String userId = UserContextHolder.getCurrentUserContext().userId();
        log.debug("[Timesheet] Fetching pending timesheets for manager | managerId={} tenantId={}", userId, tenantId);

        List<TimesheetResponse> pending = timesheetRepository
                .findSubmittedTimesheetsForManager(userId, tenantId)
                .stream()
                .map(mapper::toResponse)
                .toList();

        log.debug("[Timesheet] Pending timesheets found | managerId={} count={}", userId, pending.size());
        return pending;
    }

    @Override
    public List<TimesheetResponse> getConsultantHistory(String consultantId) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();

        log.debug("[Timesheet] Fetching history | consultantId={} tenantId={}", consultantId, tenantId);

        List<TimesheetResponse> history = timesheetRepository
                .findHistoryByConsultant(consultantId, tenantId)
                .stream()
                .map(mapper::toResponse)
                .toList();

        log.debug("[Timesheet] History fetched | consultantId={} count={}", consultantId, history.size());
        return history;
    }

    @Override
    public TimesheetResponse getTimesheet(String timesheetId) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        log.debug("[Timesheet] Fetching timesheet | timesheetId={} tenantId={}", timesheetId, tenantId);

        return timesheetRepository
                .findByIdAndTenant(timesheetId, tenantId)
                .map(mapper::toResponse)
                .orElseThrow(() -> {
                    log.warn("[Timesheet] Timesheet not found | timesheetId={} tenantId={}", timesheetId, tenantId);
                    return new IllegalArgumentException("Timesheet not found.");
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

        log.debug("[Timesheet] All timesheets fetched | tenantId={} total={}", tenantId, result.getTotalElements());
        return result;
    }


    /**
     * Only DRAFT or REJECTED timesheets can be edited
     */
    private Timesheet getEditableTimesheet(String timesheetId, String tenantId) {
        Timesheet timesheet = timesheetRepository
                .findByIdAndTenant(timesheetId, tenantId)
                .orElseThrow(() -> {
                    log.warn("[Timesheet] Timesheet not found | timesheetId={} tenantId={}", timesheetId, tenantId);
                    return new IllegalArgumentException("Timesheet not found.");
                });

        if (timesheet.getStatus() == TimesheetStatus.SUBMITTED
                || timesheet.getStatus() == TimesheetStatus.APPROVED) {
            log.warn("[Timesheet] Edit rejected — timesheet is locked | timesheetId={} status={}",
                    timesheetId, timesheet.getStatus());
            throw new IllegalStateException(
                    "Timesheet is locked. Status: " + timesheet.getStatus());
        }
        return timesheet;
    }

    private void validateDateInPeriod(LocalDate date, YearMonth period) {
        if (!YearMonth.from(date).equals(period)) {
            throw new IllegalArgumentException(
                    "Date " + date + " does not belong to period " + period);
        }
    }

    private void validateQuantity(Double quantity) {
        if (quantity != 0.0 && quantity != 0.5 && quantity != 1.0) {
            throw new IllegalArgumentException(
                    "Quantity must be 0.0 (Off), 0.5 (Half-day), or 1.0 (Full day). Got: " + quantity);
        }
    }

    private void validateDateInMissionRange(LocalDate date, Mission mission) {
        if (mission.getStartDate() != null && date.isBefore(mission.getStartDate())) {
            throw new IllegalArgumentException("Date " + date + " is before mission start.");
        }
        if (mission.getEndDate() != null && date.isAfter(mission.getEndDate())) {
            throw new IllegalArgumentException("Date " + date + " is after mission end.");
        }
    }
}