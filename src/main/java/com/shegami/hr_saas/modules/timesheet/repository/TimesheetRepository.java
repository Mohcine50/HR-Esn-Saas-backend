package com.shegami.hr_saas.modules.timesheet.repository;

import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.timesheet.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

    @Query("""
            SELECT COUNT(t) > 0
            FROM Timesheet t
            WHERE t.mission.missionId = :missionId
              AND t.consultant.consultantId = :consultantId
              AND t.month = :month
              AND t.year = :year
            """)
    boolean existsByMissionAndConsultantAndPeriod(
            @Param("missionId") String missionId,
            @Param("consultantId") String consultantId,
            @Param("month") int month,
            @Param("year") int year);

    @Query("""
            SELECT t
            FROM Timesheet t
            WHERE t.timesheetId = :timesheetId
              AND t.tenant.tenantId = :tenantId
            """)
    Optional<Timesheet> findByIdAndTenant(
            @Param("timesheetId") String timesheetId,
            @Param("tenantId") String tenantId);

    @Query("""
            SELECT t
            FROM Timesheet t
            WHERE t.consultant.consultantId = :consultantId
              AND t.tenant.tenantId = :tenantId
            ORDER BY t.year DESC, t.month DESC
            """)
    List<Timesheet> findHistoryByConsultant(
            @Param("consultantId") String consultantId,
            @Param("tenantId") String tenantId);

    @Query("""
            SELECT t FROM Timesheet t
            JOIN t.mission m
            WHERE m.accountManager.employeeId = :managerId
              AND t.tenant.tenantId = :tenantId
              AND t.status = 'SUBMITTED'
            ORDER BY t.year DESC, t.month DESC
            """)
    List<Timesheet> findSubmittedTimesheetsForManager(
            @Param("managerId") String managerId,
            @Param("tenantId") String tenantId);

    @Query("""
            SELECT t FROM Timesheet t
            WHERE t.tenant.tenantId = :tenantId
              AND t.month = :month
              AND t.year = :year
              AND t.status = 'APPROVED'
            """)
    List<Timesheet> findApprovedTimesheetsForMonth(
            @Param("tenantId") String tenantId,
            @Param("month") int month,
            @Param("year") int year);
}
