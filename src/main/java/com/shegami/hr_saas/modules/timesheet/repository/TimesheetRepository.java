package com.shegami.hr_saas.modules.timesheet.repository;

import com.shegami.hr_saas.modules.timesheet.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {
}
