package com.shegami.hr_saas.modules.mission.service;

import com.shegami.hr_saas.modules.mission.dto.MissionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MissionService {
    MissionDto createMission(MissionDto dto);
    Page<MissionDto> getMissionsByTenant(Pageable pageable);
    void terminateMission(String id);
    MissionDto updateMission(MissionDto dto);
    void deleteMission(String id);
    MissionDto getMissionById(String id);
}
