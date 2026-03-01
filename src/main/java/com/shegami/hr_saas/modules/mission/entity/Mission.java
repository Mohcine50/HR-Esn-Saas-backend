package com.shegami.hr_saas.modules.mission.entity;

import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.mission.enums.Priority;
import com.shegami.hr_saas.modules.mission.enums.MissionStatus;
import com.shegami.hr_saas.modules.upload.entity.UploadFile;
import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "missions")
public class Mission extends BaseTenantEntity {

    private String title;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private Client client;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable()
    private Set<Consultant> consultants;

    @ManyToOne(fetch = FetchType.LAZY)
    private Employee accountManager;

    @Enumerated(EnumType.STRING)
    private MissionStatus status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable()
    private Set<UploadFile> attachments;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mission_labels",
            joinColumns = @JoinColumn(name = "mission_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    private Set<Label> labels;

    private LocalDate startDate;
    private LocalDate endDate;

    @Id
    @Column(name = "mission_id", nullable = false)
    private String missionId;
    @PrePersist
    public void generateMissionId() {
        if (this.missionId == null) {
            this.missionId = "MIS-" + UUID.randomUUID();
        }
    }

}