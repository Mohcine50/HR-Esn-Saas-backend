package com.shegami.hr_saas.modules.mission.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.exception.UserNotFoundException;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.mission.dto.ClientDto;
import com.shegami.hr_saas.modules.mission.dto.MissionDto;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.mission.entity.Mission;
import com.shegami.hr_saas.modules.mission.enums.MissionStatus;
import com.shegami.hr_saas.modules.mission.mapper.ClientMapper;
import com.shegami.hr_saas.modules.mission.mapper.MissionMapper;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.mission.repository.MissionRepository;
import com.shegami.hr_saas.modules.mission.service.ClientService;
import com.shegami.hr_saas.modules.mission.service.MissionService;
import com.shegami.hr_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ClientMapper clientMapper;

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
    public int assignConsultantToMission(String consultantId, String missionId) {
        log.info("Assigning consultant {} to mission ID: {}", consultantId ,missionId);

        Consultant consultant = consultantRepository.findById(consultantId).orElseThrow(() -> new ResourceNotFoundException("Consultant not found with ID: " + consultantId));

        return missionRepository.assignConsultantToMission(missionId, consultant);
    }


    @Override
    @Transactional
    public MissionDto updateMission(MissionDto dto) {
        log.info("Updating mission ID: {}", dto.getMission_id());

        // 1. Fetch existing entity
        Mission existingMission = missionRepository.findById(dto.getMission_id())
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with ID: " + dto.getMission_id()));

        // 2. Senior Business Rule: If dates changed, re-validate availability
        boolean datesChanged = !existingMission.getStartDate().equals(dto.getStartDate()) ||
                (existingMission.getEndDate() != null && !existingMission.getEndDate().equals(dto.getEndDate()));

        if (datesChanged) {
            // Exclude current mission from the overlap check
            boolean isOverlapping = missionRepository.existsOverlapForUpdate(
                    dto.getConsultant().getConsultantId(), dto.getStartDate(), dto.getEndDate(), dto.getMission_id());

            if (isOverlapping) {
                throw new IllegalStateException("Cannot update: Consultant has another active mission during these new dates.");
            }
        }
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
    public MissionDto createMission(MissionDto dto) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        log.info("Creating mission for tenant: {}", tenantId);
        Tenant tenant = tenantService.getTenant(tenantId);

        String userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info(userEmail);
        User user = userRepository.findByEmail(userEmail).orElseThrow(
                ()-> new UserNotFoundException("User not found with email: " + userEmail));

        ClientDto client = clientService.getClientById(dto.getClientId());
        Mission mission = missionMapper.toEntity(dto);
        mission.setClient(clientMapper.toEntity(client));
        mission.setStatus(MissionStatus.ON_HOLD);
        mission.setTenant(tenant);
        mission.setAccountManager(user.getEmployee());


        return missionMapper.toDto(missionRepository.save(mission));
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
        mission.setEndDate(LocalDate.now());
        missionRepository.save(mission);
    }
}
