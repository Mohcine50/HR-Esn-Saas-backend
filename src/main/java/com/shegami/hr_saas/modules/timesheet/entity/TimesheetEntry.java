package com.shegami.hr_saas.modules.timesheet.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "timesheetentries")
public class TimesheetEntry {
    @Id
    @Column(name = "timesheet_entry_id", nullable = false)
    private String timesheetEntryId;
    @PrePersist
    public void generateTimesheetEntryId() {
        if (this.timesheetEntryId == null) {
            this.timesheetEntryId = "TMS-" + UUID.randomUUID();
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    private Timesheet timesheet;

    private LocalDate date;
    private Double quantity; // 1.0 (Day), 0.5 (Half-day), 0.0 (Off)
    private String comment; // Useful for clients who want details on what was done

}