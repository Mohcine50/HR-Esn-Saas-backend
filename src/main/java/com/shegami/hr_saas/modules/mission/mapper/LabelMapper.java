package com.shegami.hr_saas.modules.mission.mapper;

import com.shegami.hr_saas.modules.mission.dto.LabelDto;
import com.shegami.hr_saas.modules.mission.entity.Label;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface LabelMapper {
    Label toEntity(LabelDto labelDto);

    LabelDto toDto(Label label);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Label partialUpdate(LabelDto labelDto, @MappingTarget Label label);
}