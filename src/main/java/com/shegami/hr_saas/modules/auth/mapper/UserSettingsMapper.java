package com.shegami.hr_saas.modules.auth.mapper;

import com.shegami.hr_saas.modules.auth.dto.UserSettingsDto;
import com.shegami.hr_saas.modules.auth.entity.UserSettings;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserSettingsMapper {
    UserSettings toEntity(UserSettingsDto userSettingsDto);

    UserSettingsDto toDto(UserSettings userSettings);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    UserSettings partialUpdate(UserSettingsDto userSettingsDto, @MappingTarget UserSettings userSettings);
}