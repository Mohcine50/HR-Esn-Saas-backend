package com.shegami.hr_saas.modules.mission.entity;

import com.shegami.hr_saas.modules.mission.enums.ActivityType;
import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "mission_activities")
public class MissionActivity extends BaseTenantEntity {

    @Id
    @Column(name = "activity_id", nullable = false)
    private String activityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    // e.g. "status", "priority", "consultant", "label"
    private String field;

    // Human-readable: "changed status from DRAFT to ACTIVE"
    @Column(columnDefinition = "TEXT")
    private String description;

    private String fromValue; // previous value
    private String toValue;   // new value

    // Who triggered it
    @Column(nullable = false)
    private String actorId;

    @Column(nullable = false)
    private String actorName;

    @PrePersist
    public void generateId() {
        if (this.activityId == null) {
            this.activityId = "ACT-" + UUID.randomUUID();
        }
    }
}