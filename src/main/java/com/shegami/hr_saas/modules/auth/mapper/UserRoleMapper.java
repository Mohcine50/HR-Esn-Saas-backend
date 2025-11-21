package com.shegami.hr_saas.modules.auth.mapper;

import com.shegami.hr_saas.modules.auth.dto.UserDto;
import com.shegami.hr_saas.modules.auth.dto.UserRoleDto;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.entity.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserRoleMapper {
    UserRole toUserRole(UserRoleDto userRoleDto);
    UserRoleDto toUserRoleDto(UserRole userRole);
}
