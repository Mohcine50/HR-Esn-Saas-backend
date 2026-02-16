package com.shegami.hr_saas.modules.auth.mapper;

import com.shegami.hr_saas.modules.auth.dto.SecurityTokenDto;
import com.shegami.hr_saas.modules.auth.entity.SecurityToken;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SecurityTokenMapper {
    SecurityToken toEntity(SecurityTokenDto securityTokenDto);

    SecurityTokenDto toDto(SecurityToken securityToken);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    SecurityToken partialUpdate(SecurityTokenDto securityTokenDto, @MappingTarget SecurityToken securityToken);
}