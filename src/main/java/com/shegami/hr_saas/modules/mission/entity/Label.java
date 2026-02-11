package com.shegami.hr_saas.modules.mission.entity;

import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "labels")
public class Label extends BaseTenantEntity {

    private String labelName;
    private String labelDescription;

    private String color;

    @Formula("(SELECT COUNT(*) FROM mission_labels ml WHERE ml.label_id = label_id)")
    private Integer missions;

    @Id
    @Column(name = "label_id", nullable = false)
    private String labelId;
    @PrePersist
    public void generateMissionId() {
        if (this.labelId == null) {
            this.labelId = "LB-" + UUID.randomUUID();
        }
    }

}