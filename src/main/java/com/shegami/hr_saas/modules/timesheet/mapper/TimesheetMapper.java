package com.shegami.hr_saas.modules.timesheet.mapper;

import com.shegami.hr_saas.modules.auth.mapper.TenantMapper;
import com.shegami.hr_saas.modules.auth.mapper.UserMapper;
import com.shegami.hr_saas.modules.mission.mapper.MissionMapper;
import com.shegami.hr_saas.modules.timesheet.dto.TimesheetEntryResponse;
import com.shegami.hr_saas.modules.timesheet.dto.TimesheetExportRow;
import com.shegami.hr_saas.modules.timesheet.dto.TimesheetResponse;
import com.shegami.hr_saas.modules.timesheet.entity.Timesheet;
import com.shegami.hr_saas.modules.timesheet.entity.TimesheetEntry;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TimesheetMapper {

    @Mapping(source = "mission.missionId", target = "missionId")
    @Mapping(source = "mission.title",     target = "missionTitle")
    @Mapping(target = "totalDays",         expression = "java(computeTotalDays(timesheet))")
    TimesheetResponse toResponse(Timesheet timesheet);

    List<TimesheetResponse> toResponseList(List<Timesheet> timesheets);

    TimesheetEntryResponse toEntryResponse(TimesheetEntry entry);


    default double computeTotalDays(Timesheet timesheet) {
        if (timesheet.getEntries() == null) return 0.0;
        return timesheet.getEntries().stream()
                .mapToDouble(TimesheetEntry::getQuantity)
                .sum();
    }
}