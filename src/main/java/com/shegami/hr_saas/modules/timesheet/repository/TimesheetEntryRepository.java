package com.shegami.hr_saas.modules.timesheet.repository;

import com.shegami.hr_saas.modules.timesheet.entity.TimesheetEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimesheetEntryRepository extends JpaRepository<TimesheetEntry, Long> {
}
