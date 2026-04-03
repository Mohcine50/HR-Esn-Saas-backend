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

    @org.springframework.data.jpa.repository.Query("SELECT SUM(e.quantity) FROM TimesheetEntry e WHERE e.timesheet.consultant.consultantId = :consultantId AND e.timesheet.month = :month AND e.timesheet.year = :year")
    Double sumQuantityByConsultantAndMonthAndYear(
            @org.springframework.data.repository.query.Param("consultantId") String consultantId,
            @org.springframework.data.repository.query.Param("month") int month,
            @org.springframework.data.repository.query.Param("year") int year);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(e.quantity) FROM TimesheetEntry e JOIN e.timesheet t JOIN t.mission m WHERE m.accountManager.employeeId = :employeeId AND t.status = 'APPROVED' AND t.month = :month AND t.year = :year")
    Double sumApprovedQuantityByManagerAndMonthAndYear(
            @org.springframework.data.repository.query.Param("employeeId") String employeeId,
            @org.springframework.data.repository.query.Param("month") int month,
            @org.springframework.data.repository.query.Param("year") int year);
}
