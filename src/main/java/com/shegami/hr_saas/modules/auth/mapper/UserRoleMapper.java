package com.shegami.hr_saas.modules.auth.mapper;

import com.shegami.hr_saas.modules.auth.dto.UserRoleDto;
import com.shegami.hr_saas.modules.auth.entity.UserRole;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserRoleMapper {
    UserRole toUserRole(UserRoleDto userRoleDto);

    UserRoleDto toUserRoleDto(UserRole userRole);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    UserRole partialUpdate(UserRoleDto userRoleDto, @MappingTarget UserRole userRole);
}
