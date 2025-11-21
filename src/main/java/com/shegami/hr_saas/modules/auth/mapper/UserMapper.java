package com.shegami.hr_saas.modules.auth.mapper;

import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserRoleMapper.class})
public interface UserMapper {


    // Convert Entity → DTO
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", source="roles")
    UserDto toDto(User user);

    User toEntity(UserDto userDto);
}