package com.shegami.hr_saas.modules.timesheet.dto;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.shegami.hr_saas.modules.timesheet.entity.TimesheetEntry}
 */
@Value
public class TimesheetEntryDto implements Serializable {
    String timesheetEntryId;
    TimesheetDto timesheet;
    LocalDate date;
    Double quantity;
    String comment;
}