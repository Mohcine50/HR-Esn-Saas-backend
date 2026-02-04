package com.shegami.hr_saas.modules.hr.entity;


import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.hr.enums.ContractType;
import com.shegami.hr_saas.modules.hr.enums.EmployeeStatus;
import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "employees")
public class Employee extends BaseTenantEntity {


    @OneToOne(fetch = FetchType.EAGER)
    private User user;

    private String position;

    private LocalDateTime hireDate;

    private Double salary;

    private String currency;

    @Enumerated(EnumType.STRING)
    private ContractType contractType;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    private String bio;

    private String imageUrl;

    @Id
    @Column(name = "employee_id", nullable = false)
    private String employeeId;
    @PrePersist
    public void generateEmployeeId() {
        if (this.employeeId == null) {
            this.employeeId = "EMP-" + UUID.randomUUID();
        }
    }

}
