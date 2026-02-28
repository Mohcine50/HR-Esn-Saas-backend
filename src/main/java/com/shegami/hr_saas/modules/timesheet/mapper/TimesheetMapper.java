package com.shegami.hr_saas.modules.timesheet.mapper;

import com.shegami.hr_saas.modules.auth.mapper.TenantMapper;
import com.shegami.hr_saas.modules.auth.mapper.UserMapper;
import com.shegami.hr_saas.modules.mission.mapper.MissionMapper;
import com.shegami.hr_saas.modules.timesheet.dto.TimesheetDto;
import com.shegami.hr_saas.modules.timesheet.entity.Timesheet;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {TenantMapper.class, UserMapper.class, UserMapper.class, MissionMapper.class, TimesheetEntryMapper.class})
public interface TimesheetMapper {
    Timesheet toEntity(TimesheetDto timesheetDto);

    @AfterMapping
    default void linkEntries(@MappingTarget Timesheet timesheet) {
        timesheet.getEntries().forEach(entry -> entry.setTimesheet(timesheet));
    }

    TimesheetDto toDto(Timesheet timesheet);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Timesheet partialUpdate(TimesheetDto timesheetDto, @MappingTarget Timesheet timesheet);
}