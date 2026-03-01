package com.shegami.hr_saas.modules.timesheet.mapper;

import com.shegami.hr_saas.modules.timesheet.entity.TimesheetEntry;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {TimesheetMapper.class})
public interface TimesheetEntryMapper {
    TimesheetEntry toEntity(TimesheetEntryDto timesheetEntryDto);

    TimesheetEntryDto toDto(TimesheetEntry timesheetEntry);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    TimesheetEntry partialUpdate(TimesheetEntryDto timesheetEntryDto, @MappingTarget TimesheetEntry timesheetEntry);
}