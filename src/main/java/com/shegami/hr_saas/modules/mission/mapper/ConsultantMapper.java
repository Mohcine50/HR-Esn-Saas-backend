package com.shegami.hr_saas.modules.mission.mapper;

import com.shegami.hr_saas.modules.auth.mapper.TenantMapper;
import com.shegami.hr_saas.modules.auth.mapper.UserMapper;
import com.shegami.hr_saas.modules.mission.dto.ConsultantDto;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {TenantMapper.class, UserMapper.class})
public interface ConsultantMapper {
    Consultant toEntity(ConsultantDto consultantDto);

    ConsultantDto toDto(Consultant consultant);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Consultant partialUpdate(ConsultantDto consultantDto, @MappingTarget Consultant consultant);
}