package com.shegami.hr_saas.modules.mission.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.exception.UserNotFoundException;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.mission.dto.MissionDto;
import com.shegami.hr_saas.modules.mission.dto.NewMissionRequest;
import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.mission.entity.Mission;
import com.shegami.hr_saas.modules.mission.entity.Project;
import com.shegami.hr_saas.modules.mission.enums.ActivityType;
import com.shegami.hr_saas.modules.mission.enums.MissionStatus;
import com.shegami.hr_saas.modules.mission.exceptions.ConsultantNotFoundException;
import com.shegami.hr_saas.modules.mission.exceptions.MissionNotFoundException;
import com.shegami.hr_saas.modules.mission.exceptions.ProjectNotFoundException;
import com.shegami.hr_saas.modules.mission.mapper.MissionMapper;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.mission.repository.MissionRepository;
import com.shegami.hr_saas.modules.mission.repository.ProjectRepository;
import com.shegami.hr_saas.modules.mission.service.*;
import com.shegami.hr_saas.modules.upload.service.UploadService;
import com.shegami.hr_saas.shared.exception.ResourceNotFoundException;
import com.shegami.hr_saas.config.domain.rabbitMq.RabbitMQConfig;
import com.shegami.hr_saas.modules.notifications.dto.NotificationMessage;
import com.shegami.hr_saas.modules.notifications.enums.EntityType;
import com.shegami.hr_saas.modules.notifications.enums.NotificationType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class MissionServiceImpl implements MissionService {

        private final MissionRepository missionRepository;
        private final MissionMapper missionMapper;
        private final ConsultantRepository consultantRepository;
        private final TenantService tenantService;
        private final UserRepository userRepository;
        private final ClientService clientService;
        private final ConsultantService consultantService;
        private final UploadService uploadService;
        private final LabelsService labelsService;
        private final ProjectRepository projectRepository;
        private final MissionActivityService activityService;
        private final RabbitTemplate rabbitTemplate;

        // ── Read ──────────────────────────────────────────────────────────────────

        @Override
        @Transactional(readOnly = true)
        public MissionDto getMissionById(String id) {
                log.debug("[Mission] Fetching mission | missionId={}", id);

                return missionRepository.findById(id)
                                .map(missionMapper::toDto)
                                .orElseThrow(() -> {
                                        log.warn("[Mission] Mission not found | missionId={}", id);
                                        return new ResourceNotFoundException("Mission not found with ID: " + id);
                                });
        }

        @Override
        @Transactional(readOnly = true)
        public Page<MissionDto> getMissionByConsultant(Pageable pageable) {
                String userId = UserContextHolder.getCurrentUserContext().userId();
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();

                log.debug("[Mission] Fetching missions for consultant | userId={} tenantId={}", userId, tenantId);

                Consultant consultant = consultantRepository.findByUserUserId(userId)
                                .orElseThrow(() -> {
                                        log.warn("[Mission] Consultant not found for user | userId={}", userId);
                                        return new ConsultantNotFoundException(
                                                        "Consultant not found with userId: " + userId);
                                });

                Page<MissionDto> page = missionRepository
                                .findByConsultantIdAndTenantId(pageable, tenantId, consultant)
                                .map(missionMapper::toDto);

                log.debug("[Mission] Consultant missions fetched | userId={} count={} page={}",
                                userId, page.getTotalElements(), pageable.getPageNumber());

                return page;
        }

        @Override
        @Transactional(readOnly = true)
        public Page<MissionDto> getMissionsByTenant(Pageable pageable) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();

                log.debug("[Mission] Fetching all missions for tenant | tenantId={} page={}",
                                tenantId, pageable.getPageNumber());

                Page<MissionDto> page = missionRepository
                                .findByTenantId(pageable, tenantId)
                                .map(missionMapper::toDto);

                log.debug("[Mission] Tenant missions fetched | tenantId={} total={}", tenantId,
                                page.getTotalElements());
                return page;
        }

        // ── Create ────────────────────────────────────────────────────────────────

        @Override
        @Transactional
        public MissionDto createMission(NewMissionRequest dto) {
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
                String actorId = UserContextHolder.getCurrentUserContext().userId();

                log.info("[Mission] Creating mission | tenantId={} title={} projectId={} clientId={}",
                                tenantId, dto.getTitle(), dto.getProject(), dto.getClient());

                Tenant tenant = tenantService.getTenant(tenantId);

                String userEmail = UserContextHolder.getCurrentUserContext().email();
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> {
                                        log.warn("[Mission] Authenticated user not found | email={}", userEmail);
                                        return new UserNotFoundException("User not found with email: " + userEmail);
                                });

                var consultants = consultantService.getAllConsultants(dto.getConsultants());
                var attachements = uploadService.getUploadFiles(dto.getAttachements());

                Project project = projectRepository.findById(dto.getProject())
                                .orElseThrow(() -> {
                                        log.warn("[Mission] Project not found | projectId={}", dto.getProject());
                                        return new ProjectNotFoundException(
                                                        "Project not found with id: " + dto.getProject());
                                });

                consultants.forEach(project::addConsultant);
                Project savedProject = projectRepository.save(project);

                Client resolvedClient = clientService.getClientByIdForMission(dto.getClient());
                if (resolvedClient == null && project.getClient() != null) {
                        resolvedClient = project.getClient();
                }
                final Client finalClient = resolvedClient;

                var labels = labelsService.getAllLabels(dto.getLabels());

                Mission mission = Mission.builder()
                                .client(finalClient)
                                .status(dto.getStatus())
                                .title(dto.getTitle())
                                .description(dto.getDescription())
                                .priority(dto.getPriority())
                                .project(savedProject)
                                .consultants(consultants)
                                .labels(labels)
                                .attachments(attachements)
                                .build();
                mission.setTenant(tenant);
                mission.setAccountManager(user.getEmployee());

                Mission saved = missionRepository.save(mission);

                String actorName = user.getFullName();

                // ── Activity: created ─────────────────────────────────────────────────
                activityService.log(
                                saved,
                                ActivityType.MISSION_CREATED,
                                "created this mission",
                                actorId,
                                actorName);

                // ── Activity: initial status ──────────────────────────────────────────
                activityService.log(
                                saved,
                                ActivityType.STATUS_CHANGED,
                                "status", null, dto.getStatus().name(),
                                actorId, actorName);

                // ── Activity: consultants assigned at creation ────────────────────────
                consultants.forEach(c -> {
                        activityService.log(
                                        saved,
                                        ActivityType.CONSULTANT_ASSIGNED,
                                        "consultant", null,
                                        c.getFirstName() + " " + c.getLastName(),
                                        actorId, actorName);

                        NotificationMessage msg = NotificationMessage.builder()
                                        .userId(c.getUser().getUserId())
                                        .notificationType(NotificationType.CONSULTANT_ASSIGNED)
                                        .title(NotificationType.CONSULTANT_ASSIGNED.getDefaultTitle())
                                        .message("You have been assigned to the mission: " + saved.getTitle())
                                        .entityType(EntityType.MISSION)
                                        .entityId(saved.getMissionId())
                                        .actorId(actorId)
                                        .actorName(actorName)
                                        .metadata(Map.of(
                                                        "missionTitle", saved.getTitle(),
                                                        "missionId", saved.getMissionId(),
                                                        "clientName",
                                                        finalClient != null ? finalClient.getFullName() : "N/A",
                                                        "startDate",
                                                        saved.getStartDate() != null ? saved.getStartDate().toString()
                                                                        : "N/A",
                                                        "endDate",
                                                        saved.getEndDate() != null ? saved.getEndDate().toString()
                                                                        : "N/A",
                                                        "priority", saved.getPriority().name()))
                                        .build();
                        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                        "notification.mission.assigned",
                                        msg);
                });

                // ── Activity: labels added at creation ────────────────────────────────
                labels.forEach(l -> activityService.log(
                                saved,
                                ActivityType.LABEL_ADDED,
                                "label", null, l.getLabelName(),
                                actorId, actorName));

                log.info("[Mission] Mission created | missionId={} title={} tenantId={} consultants={} labels={}",
                                saved.getMissionId(), saved.getTitle(), tenantId,
                                consultants.size(), labels.size());

                return missionMapper.toDto(saved);
        }

        // ── Update ────────────────────────────────────────────────────────────────

        @Override
        @Transactional
        public MissionDto updateMission(MissionDto dto, String missionId) {
                String actorId = UserContextHolder.getCurrentUserContext().userId();
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();

                log.info("[Mission] Updating mission | missionId={} actorId={} for tenant {}", missionId, actorId,
                                tenantId);

                Mission existing = missionRepository.findById(missionId)
                                .orElseThrow(() -> {
                                        log.warn("[Mission] Mission not found for update | missionId={}",
                                                        dto.getMissionId());
                                        return new MissionNotFoundException(
                                                        "Mission not found with ID: " + dto.getMissionId());
                                });

                String actorName = resolveActorName(actorId);

                // ── Detect and log field-level changes ────────────────────────────────

                if (dto.getStatus() != null && !dto.getStatus().equals(existing.getStatus())) {
                        log.info("[Mission] Status change | missionId={} from={} to={} actorId={}",
                                        existing.getMissionId(), existing.getStatus(), dto.getStatus(), actorId);

                        MissionStatus oldStatus = existing.getStatus();
                        MissionStatus newStatus = dto.getStatus();

                        activityService.log(
                                        existing,
                                        ActivityType.STATUS_CHANGED,
                                        "status",
                                        oldStatus.name(),
                                        newStatus.name(),
                                        actorId, actorName);

                        sendMissionStatusChangedNotification(existing, oldStatus, newStatus, actorId, actorName);
                }

                if (dto.getPriority() != null && !dto.getPriority().equals(existing.getPriority())) {
                        log.info("[Mission] Priority change | missionId={} from={} to={} actorId={}",
                                        existing.getMissionId(), existing.getPriority(), dto.getPriority(), actorId);
                        activityService.log(
                                        existing,
                                        ActivityType.PRIORITY_CHANGED,
                                        "priority",
                                        existing.getPriority() != null ? existing.getPriority().name() : null,
                                        dto.getPriority().name(),
                                        actorId, actorName);
                }

                if (dto.getTitle() != null && !dto.getTitle().equals(existing.getTitle())) {
                        log.info("[Mission] Title change | missionId={} actorId={}", existing.getMissionId(), actorId);
                        activityService.log(
                                        existing,
                                        ActivityType.TITLE_CHANGED,
                                        "title",
                                        existing.getTitle(),
                                        dto.getTitle(),
                                        actorId, actorName);
                }

                if (dto.getDescription() != null && !dto.getDescription().equals(existing.getDescription())) {
                        log.info("[Mission] Description updated | missionId={} actorId={}", existing.getMissionId(),
                                        actorId);
                        activityService.log(
                                        existing,
                                        ActivityType.DESCRIPTION_CHANGED,
                                        "description", null, null,
                                        actorId, actorName);
                }

                if (dto.getStartDate() != null && !dto.getStartDate().equals(existing.getStartDate())
                                || dto.getEndDate() != null && !dto.getEndDate().equals(existing.getEndDate())) {
                        log.info("[Mission] Dates updated | missionId={} startDate={} endDate={} actorId={}",
                                        existing.getMissionId(), dto.getStartDate(), dto.getEndDate(), actorId);
                        activityService.log(
                                        existing,
                                        ActivityType.DATE_CHANGED,
                                        "dates",
                                        formatDateRange(existing.getStartDate(), existing.getEndDate()),
                                        formatDateRange(dto.getStartDate(), dto.getEndDate()),
                                        actorId, actorName);
                }

                missionMapper.partialUpdate(dto, existing);
                Mission updated = missionRepository.save(existing);

                log.info("[Mission] Mission updated | missionId={} actorId={}", updated.getMissionId(), actorId);
                return missionMapper.toDto(updated);
        }

        // ── Assign consultant ─────────────────────────────────────────────────────

        @Override
        @Transactional
        public void assignConsultantToMission(String consultantId, String missionId) {
                String actorId = UserContextHolder.getCurrentUserContext().userId();
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();

                log.info("[Mission] Assigning consultant | missionId={} consultantId={} actorId={}",
                                missionId, consultantId, actorId);

                Mission mission = missionRepository.findById(missionId)
                                .orElseThrow(() -> {
                                        log.warn("[Mission] Mission not found for assignment | missionId={}",
                                                        missionId);
                                        return new ResourceNotFoundException("Mission not found with ID: " + missionId);
                                });

                Consultant consultant = consultantRepository.findById(consultantId)
                                .orElseThrow(() -> {
                                        log.warn("[Mission] Consultant not found | consultantId={}", consultantId);
                                        return new ResourceNotFoundException(
                                                        "Consultant not found with ID: " + consultantId);
                                });

                if (mission.getConsultants().contains(consultant)) {
                        log.warn("[Mission] Consultant already assigned — skipping | missionId={} consultantId={}",
                                        missionId, consultantId);
                        return;
                }

                mission.getConsultants().add(consultant);

                String consultantFullName = consultant.getFirstName() + " " + consultant.getLastName();
                String actorName = resolveActorName(actorId);

                activityService.log(
                                mission,
                                ActivityType.CONSULTANT_ASSIGNED,
                                "consultant", null, consultantFullName,
                                actorId, actorName);

                NotificationMessage msg = NotificationMessage.builder()
                                .userId(consultant.getUser().getUserId())
                                .notificationType(NotificationType.CONSULTANT_ASSIGNED)
                                .title(NotificationType.CONSULTANT_ASSIGNED.getDefaultTitle())
                                .message("You have been assigned to the mission: " + mission.getTitle())
                                .entityType(EntityType.MISSION)
                                .entityId(missionId)
                                .actorId(actorId)
                                .actorName(actorName)
                                .metadata(Map.of(
                                                "missionTitle", mission.getTitle(),
                                                "missionId", mission.getMissionId(),
                                                "clientName",
                                                mission.getClient() != null ? mission.getClient().getFullName() : "N/A",
                                                "startDate",
                                                mission.getStartDate() != null ? mission.getStartDate().toString()
                                                                : "N/A",
                                                "endDate",
                                                mission.getEndDate() != null ? mission.getEndDate().toString() : "N/A",
                                                "priority", mission.getPriority().name()))
                                .build();
                rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, "notification.mission.assigned",
                                msg);

                log.info("[Mission] Consultant assigned | missionId={} consultantId={} name={}",
                                missionId, consultantId, consultantFullName);
        }

        // ── Terminate ─────────────────────────────────────────────────────────────

        @Override
        @Transactional
        public void terminateMission(String id) {
                String actorId = UserContextHolder.getCurrentUserContext().userId();
                String tenantId = UserContextHolder.getCurrentUserContext().tenantId();

                log.info("[Mission] Terminating mission | missionId={} actorId={}", id, actorId);

                Mission mission = missionRepository.findById(id)
                                .orElseThrow(() -> {
                                        log.warn("[Mission] Mission not found for termination | missionId={}", id);
                                        return new ResourceNotFoundException("Mission not found with ID: " + id);
                                });

                MissionStatus previousStatus = mission.getStatus();
                mission.setStatus(MissionStatus.COMPLETED);
                missionRepository.save(mission);

                String actorName = resolveActorName(actorId);

                activityService.log(
                                mission,
                                ActivityType.STATUS_CHANGED,
                                "status",
                                previousStatus.name(),
                                MissionStatus.COMPLETED.name(),
                                actorId,
                                actorName);

                activityService.log(
                                mission,
                                ActivityType.MISSION_DELETED, // or add MISSION_TERMINATED to enum
                                "terminated this mission",
                                actorId, actorName);

                log.info("[Mission] Mission terminated | missionId={} previousStatus={} actorId={}",
                                id, previousStatus, actorId);
        }

        // ── Delete ────────────────────────────────────────────────────────────────

        @Override
        @Transactional
        public void deleteMission(String id) {
                String actorId = UserContextHolder.getCurrentUserContext().userId();

                log.warn("[Mission] Deleting mission | missionId={} actorId={}", id, actorId);

                Mission mission = missionRepository.findById(id)
                                .orElseThrow(() -> {
                                        log.warn("[Mission] Mission not found for deletion | missionId={}", id);
                                        return new ResourceNotFoundException("Mission not found with ID: " + id);
                                });

                // log before delete so the FK still exists
                activityService.log(
                                mission,
                                ActivityType.MISSION_DELETED,
                                "deleted this mission",
                                actorId,
                                resolveActorName(actorId));

                missionRepository.deleteById(id);

                log.warn("[Mission] Mission deleted | missionId={} title={} actorId={}",
                                id, mission.getTitle(), actorId);
        }

        // ── Helpers ───────────────────────────────────────────────────────────────

        /**
         * Resolve a display name for the actor from the consultant/employee record.
         * Falls back gracefully to the userId if not found.
         */
        private String resolveActorName(String userId) {
                try {
                        return consultantRepository.findByUserUserId(userId)
                                        .map(c -> c.getFirstName() + " " + c.getLastName())
                                        .orElse(userId);
                } catch (Exception e) {
                        log.debug("[Mission] Could not resolve actor name | userId={}", userId);
                        return userId;
                }
        }

        private String formatDateRange(LocalDate start, LocalDate end) {
                if (start == null && end == null)
                        return "—";
                return "%s → %s".formatted(
                                start != null ? start.toString() : "?",
                                end != null ? end.toString() : "?");
        }

        private void sendMissionStatusChangedNotification(Mission mission, MissionStatus from, MissionStatus to,
                        String actorId, String actorName) {
                // Notify consultants
                mission.getConsultants().forEach(c -> {
                        NotificationMessage msg = NotificationMessage.builder()
                                        .userId(c.getUser().getUserId())
                                        .notificationType(NotificationType.MISSION_STATUS_CHANGED)
                                        .message(String.format("Mission '%s' status changed to %s", mission.getTitle(),
                                                        to))
                                        .entityType(EntityType.MISSION)
                                        .entityId(mission.getMissionId())
                                        .actorId(actorId)
                                        .actorName(actorName)
                                        .metadata(Map.of(
                                                        "missionTitle", mission.getTitle(),
                                                        "missionId", mission.getMissionId(),
                                                        "fromStatus", from.name(),
                                                        "toStatus", to.name(),
                                                        "clientName", mission.getClient().getFullName()))
                                        .build();

                        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                        "notification.mission.status_changed", msg);
                });

                // Notify account manager if not the actor
                if (mission.getAccountManager() != null && mission.getAccountManager().getUser() != null
                                && !mission.getAccountManager().getUser().getUserId().equals(actorId)) {
                        NotificationMessage msg = NotificationMessage.builder()
                                        .userId(mission.getAccountManager().getUser().getUserId())
                                        .notificationType(NotificationType.MISSION_STATUS_CHANGED)
                                        .message(String.format("Mission '%s' status changed to %s", mission.getTitle(),
                                                        to))
                                        .entityType(EntityType.MISSION)
                                        .entityId(mission.getMissionId())
                                        .actorId(actorId)
                                        .actorName(actorName)
                                        .metadata(Map.of(
                                                        "missionTitle", mission.getTitle(),
                                                        "missionId", mission.getMissionId(),
                                                        "fromStatus", from.name(),
                                                        "toStatus", to.name(),
                                                        "clientName", mission.getClient().getFullName()))
                                        .build();

                        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                        "notification.mission.status_changed", msg);
                }
        }
}