package com.shegami.hr_saas.modules.mission.service.implementations;

import com.shegami.hr_saas.config.domain.context.TenantContextHolder;
import com.shegami.hr_saas.modules.mission.dto.MissionDto;
import com.shegami.hr_saas.modules.mission.entity.Mission;
import com.shegami.hr_saas.modules.mission.enums.MissionStatus;
import com.shegami.hr_saas.modules.mission.mapper.MissionMapper;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.mission.repository.MissionRepository;
import com.shegami.hr_saas.modules.mission.service.MissionService;
import com.shegami.hr_saas.shared.exception.AlreadyExistsException;
import com.shegami.hr_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    @Transactional(readOnly = true)
    public MissionDto getMissionById(String id) {
        log.debug("Fetching mission by ID: {}", id);
        return missionRepository.findById(id)
                .map(missionMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with ID: " + id));
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
        log.info("Creating mission for consultant {} at client {}", dto.getConsultant().getConsultantId(), dto.getClient().getClientId());

        boolean isBusy = missionRepository.existsByConsultantIdAndDateRange(
                dto.getConsultant().getConsultantId(), dto.getStartDate(), dto.getEndDate());
        if (isBusy) {
            throw new AlreadyExistsException("Consultant is already assigned to another mission during these dates.");
        }

        Mission mission = missionMapper.toEntity(dto);
        mission.setStatus(MissionStatus.ACTIVE); // Default to active on creation

        return missionMapper.toDto(missionRepository.save(mission));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MissionDto> getMissionsByTenant(Pageable pageable) {
        String tenantId = TenantContextHolder.getCurrentTenant();
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
