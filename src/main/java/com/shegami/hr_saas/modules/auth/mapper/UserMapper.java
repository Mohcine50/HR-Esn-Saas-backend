package com.shegami.hr_saas.modules.auth.mapper;

import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {


    // Convert Entity → DTO
    UserDto toDto(User user);

    // Convert DTO → Entity
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserDto userDto);
}