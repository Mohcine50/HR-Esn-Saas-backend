package com.shegami.hr_saas.modules.hr.mapper;

import com.shegami.hr_saas.modules.auth.mapper.TenantMapper;
import com.shegami.hr_saas.modules.auth.mapper.UserMapper;
import com.shegami.hr_saas.modules.hr.dto.InvitationDto;
import com.shegami.hr_saas.modules.hr.entity.Invitation;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {TenantMapper.class, UserMapper.class})
public interface InvitationMapper {
    Invitation toEntity(InvitationDto invitationDto);

    InvitationDto toDto(Invitation invitation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Invitation partialUpdate(InvitationDto invitationDto, @MappingTarget Invitation invitation);
}