package com.shegami.hr_saas.modules.mission.service;

import com.shegami.hr_saas.modules.mission.dto.ConsultantDto;
import com.shegami.hr_saas.modules.mission.dto.MissionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface MissionService {
    MissionDto createMission(MissionDto dto);
    Page<MissionDto> getMissionsByTenant(Pageable pageable);
    void terminateMission(String id);

    int assignConsultantToMission(String consultantId, String missionId);

    MissionDto updateMission(MissionDto dto);
    void deleteMission(String id);
    MissionDto getMissionById(String id);
    Page<MissionDto> getMissionByConsultant(Pageable pageable);

}
