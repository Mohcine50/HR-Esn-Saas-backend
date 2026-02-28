package com.shegami.hr_saas.modules.mission.entity;

import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.mission.enums.Priority;
import com.shegami.hr_saas.modules.mission.enums.ProjectStatus;
import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
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
    private Set<Consultant> consultants = new HashSet<>();

    @OneToMany(mappedBy = "project")
    private Set<Mission> missions = new HashSet<>();

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

    public void addConsultant(Consultant consultant) {
        this.consultants.add(consultant);
        consultant.getProjects().add(this);
    }

    public void removeConsultant(Consultant consultant) {
        this.consultants.remove(consultant);
        consultant.getProjects().remove(this);
    }

}