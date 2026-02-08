package com.shegami.hr_saas.modules.auth.mapper;

import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.dto.UserDto;
import com.shegami.hr_saas.modules.auth.entity.UserSettings;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserRoleMapper.class, UserSettings.class})
public interface UserMapper {


    // Convert Entity → DTO
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", source = "roles")
    UserDto toDto(User user);

    User toEntity(UserDto userDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User partialUpdate(UserDto userDto, @MappingTarget User user);
}