package com.shegami.hr_saas.modules.timesheet.repository;

import com.shegami.hr_saas.modules.timesheet.entity.TimesheetEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimesheetEntryRepository extends JpaRepository<TimesheetEntry, Long> {
    boolean existsByTimesheet_TimesheetIdAndDate(String timesheetId, LocalDate date);

    List<TimesheetEntry> findByTimesheet_TimesheetIdOrderByDateAsc(String timesheetId);
}
