package com.shegami.hr_saas.modules.mission.mapper;

import com.shegami.hr_saas.modules.auth.mapper.TenantMapper;
import com.shegami.hr_saas.modules.mission.dto.MissionDto;
import com.shegami.hr_saas.modules.mission.entity.Mission;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ClientMapper.class, MissionMapper.class, ConsultantMapper.class, TenantMapper.class})
public interface MissionMapper {
    Mission toEntity(MissionDto missionDto);

    @Mapping(target = "project.missions", ignore = true)
    MissionDto toDto(Mission mission);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Mission partialUpdate(MissionDto missionDto, @MappingTarget Mission mission);
}