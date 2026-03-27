package com.shegami.hr_saas.modules.timesheet.service;

import com.shegami.hr_saas.modules.timesheet.dto.CreateTimesheetRequest;
import com.shegami.hr_saas.modules.timesheet.dto.ReviewTimesheetRequest;
import com.shegami.hr_saas.modules.timesheet.dto.SaveEntriesRequest;
import com.shegami.hr_saas.modules.timesheet.dto.TimesheetResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TimesheetService {
    TimesheetResponse createTimesheet(CreateTimesheetRequest req);

    TimesheetResponse saveEntries(String timesheetId, SaveEntriesRequest req);

    TimesheetResponse submitTimesheet(String timesheetId);

    TimesheetResponse reviewTimesheet(
            String timesheetId,
            ReviewTimesheetRequest req);

    List<TimesheetResponse> getPendingTimesheets();

    List<TimesheetResponse> getConsultantHistory(String consultantId);

    TimesheetResponse getTimesheet(String timesheetId);

    Page<TimesheetResponse> getAllTimesheets(Pageable pageable);

    List<TimesheetResponse> getApprovedTimesheetsByClient(String clientId);
}
