package com.shegami.hr_saas.modules.mission.entity;

import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.mission.enums.Priority;
import com.shegami.hr_saas.modules.mission.enums.ProjectStatus;
import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "projects",
indexes = @Index(columnList = "tenant_id, name")
)
public class Project extends BaseTenantEntity {

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @ElementCollection
    private Set<String> tags;

    @ManyToMany
    @JoinTable()
    private Set<Consultant> consultants;

    @OneToMany
    @JoinTable()
    private Set<Mission> missions;

    @Enumerated(EnumType.STRING)
    private ProjectStatus projectStatus;

    @ManyToOne()
    private Client client;

    @Id
    @Column(name = "project_id", nullable = false)
    private String projectId;
    @PrePersist
    public void generateId() {
        if (this.projectId == null) {
            this.projectId = "PRJ-" + UUID.randomUUID();
        }
    }

}