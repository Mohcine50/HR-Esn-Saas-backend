package com.shegami.hr_saas.modules.mission.entity;

import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.mission.enums.ConsultantLevel;
import com.shegami.hr_saas.modules.mission.enums.ConsultantStatus;
import com.shegami.hr_saas.modules.mission.enums.ConsultantType;
import com.shegami.hr_saas.modules.timesheet.entity.Timesheet;
import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "consultants", indexes = {@Index(columnList = "tenant_id, firstName"),
        @Index(columnList = "tenant_id, lastName")
}
)
public class Consultant extends BaseTenantEntity {
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "profile_title")
    private String profileTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsultantType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsultantStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal internalDailyCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsultantLevel seniority;

    @ManyToMany
    private Set<Project> projects;

    @ElementCollection
    @CollectionTable(name = "consultant_skills", joinColumns = @JoinColumn(name = "consultant_id"))
    @Column(name = "skill")
    private Set<String> skills = new HashSet<>();

    private String cvS3Key;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<Mission> missions;

    @OneToMany
    private List<Timesheet> timesheets;

    @Id
    @Column(name = "consultant_id", nullable = false)
    private String consultantId;
    @PrePersist
    public void generateConsultantId() {
        if (this.consultantId == null) {
            this.consultantId = "CLT-" + UUID.randomUUID();
        }
    }

}


