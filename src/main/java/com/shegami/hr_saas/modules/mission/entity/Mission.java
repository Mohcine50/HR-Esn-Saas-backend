package com.shegami.hr_saas.modules.mission.entity;

import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.mission.enums.MissionPriority;
import com.shegami.hr_saas.modules.mission.enums.MissionStatus;
import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "missions")
public class Mission extends BaseTenantEntity {

    private String title;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    private Consultant consultant;

    @ManyToOne(fetch = FetchType.LAZY)
    private Employee accountManager;

    @Column(nullable = false)
    private BigDecimal dailyRate;

    @Column(nullable = false)
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private MissionStatus status;

    @Enumerated(EnumType.STRING)
    private MissionPriority priority;

    @Id
    @Column(name = "mission_id", nullable = false)
    private String mission_id;
    @PrePersist
    public void generateMissionId() {
        if (this.mission_id == null) {
            this.mission_id = "MIS-" + UUID.randomUUID();
        }
    }

}