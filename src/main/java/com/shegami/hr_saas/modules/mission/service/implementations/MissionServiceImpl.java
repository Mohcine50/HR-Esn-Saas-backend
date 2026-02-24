package com.shegami.hr_saas.modules.mission.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.exception.UserNotFoundException;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.mission.dto.ClientDto;
import com.shegami.hr_saas.modules.mission.dto.MissionDto;
import com.shegami.hr_saas.modules.mission.dto.NewMissionRequest;
import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.mission.entity.Mission;
import com.shegami.hr_saas.modules.mission.entity.Project;
import com.shegami.hr_saas.modules.mission.enums.MissionStatus;
import com.shegami.hr_saas.modules.mission.mapper.ClientMapper;
import com.shegami.hr_saas.modules.mission.mapper.ConsultantMapper;
import com.shegami.hr_saas.modules.mission.mapper.MissionMapper;
import com.shegami.hr_saas.modules.mission.mapper.ProjectMapper;
import com.shegami.hr_saas.modules.mission.repository.ClientRepository;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.mission.repository.MissionRepository;
import com.shegami.hr_saas.modules.mission.service.*;
import com.shegami.hr_saas.modules.upload.entity.UploadFile;
import com.shegami.hr_saas.modules.upload.service.UploadService;
import com.shegami.hr_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;

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
    private final ClientMapper clientMapper;
    private final ProjectService projectService;
    private final ProjectMapper projectMapper;
    private final ConsultantService consultantService;
    private final UploadService uploadService;
    private final LabelsService labelsService;

    @Override
    @Transactional(readOnly = true)
    public MissionDto getMissionById(String id) {
        log.debug("Fetching mission by ID: {}", id);
        return missionRepository.findById(id)
                .map(missionMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MissionDto> getMissionByConsultant(Pageable pageable) {
        String userId = UserContextHolder.getCurrentUserContext().userId();
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();

        log.info("Fetching missions for Consultant : {} From Tenant : {}", userId, tenantId );

        return missionRepository.findByConsultantIdAndTenantId(pageable, tenantId, userId).map(missionMapper::toDto);
    }

    @Transactional
    @Override
    public void assignConsultantToMission(String consultantId, String missionId) {
        log.info("Assigning consultant {} to mission {}", consultantId, missionId);

        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with ID: " + missionId));

        Consultant consultant = consultantRepository.findById(consultantId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant not found with ID: " + consultantId));

        if (mission.getConsultants().contains(consultant)) {
            log.warn("Consultant {} is already assigned to mission {}", consultantId, missionId);
            return;
        }

        mission.getConsultants().add(consultant);

        log.info("Successfully assigned consultant {} to mission {}", consultantId, missionId);
    }


    @Override
    @Transactional
    public MissionDto updateMission(MissionDto dto) {
        log.info("Updating mission ID: {}", dto.getMissionId());

        // 1. Fetch existing entity
        Mission existingMission = missionRepository.findById(dto.getMissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with ID: " + dto.getMissionId()));

        missionMapper.partialUpdate(dto, existingMission);

        Mission updatedMission = missionRepository.save(existingMission);
        return missionMapper.toDto(updatedMission);
    }

    @Override
    @Transactional
    public void deleteMission(String id) {
        log.warn("Attempting to delete mission ID: {}", id);

        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with ID: " + id));

        missionRepository.deleteById(id);

        log.info("Mission {} successfully soft-deleted", id);
    }

    @Override
    @Transactional
    public MissionDto createMission(NewMissionRequest dto) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        log.info("Creating mission for tenant: {}", tenantId);
        Tenant tenant = tenantService.getTenant(tenantId);

        String userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userEmail).orElseThrow(
                ()-> new UserNotFoundException("User not found with email: " + userEmail));

        var consultants = consultantService.getAllConsultants(dto.getConsultants());
        var attachements = uploadService.getUploadFiles(dto.getAttachements());

        Project project = projectMapper.toEntity(projectService.getProjectById(dto.getProject()));
        Client client = clientService.getClientByIdForMission(dto.getClient());
        var labels = labelsService.getAllLabels(dto.getLabels());

        Mission mission = Mission.builder()
                .client(client)
                .status(dto.getStatus())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .priority(dto.getPriority())
                .project(project)
                .consultants(consultants)
                .labels(labels)
                .attachments(attachements)
                .build();
        mission.setTenant(tenant);
        mission.setAccountManager(user.getEmployee());
        var savedMission = missionRepository.save(mission);
        log.info("Mission successfully created with ID: {}", savedMission.getMissionId());

        return missionMapper.toDto(savedMission);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MissionDto> getMissionsByTenant(Pageable pageable) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        return missionRepository.findByTenantId(pageable, tenantId)
                .map(missionMapper::toDto);
    }

    @Override
    @Transactional
    public void terminateMission(String id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found"));

        mission.setStatus(MissionStatus.COMPLETED);
        missionRepository.save(mission);
    }
}
