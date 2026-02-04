package com.shegami.hr_saas.modules.timesheet.entity;

import com.shegami.hr_saas.modules.mission.entity.Mission;
import com.shegami.hr_saas.modules.timesheet.enums.TimesheetStatus;
import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "timesheets")
public class Timesheet extends BaseTenantEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Mission mission;

    private Integer month;
    private Integer year;

    @Enumerated(EnumType.STRING)
    private TimesheetStatus status;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimesheetEntry> entries;

    private LocalDateTime validatedAt;
    private String validatedBy;

    @Id
    @Column(name = "timesheet_id", nullable = false)
    private String timesheetId;
    @PrePersist
    public void generateTimesheetId() {
        if (this.timesheetId == null) {
            this.timesheetId = "TMS-" + UUID.randomUUID();
        }
    }

}