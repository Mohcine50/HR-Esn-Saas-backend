package com.shegami.hr_saas.modules.hr.entity;


import com.shegami.hr_saas.modules.hr.enums.ContractStatus;
import com.shegami.hr_saas.modules.hr.enums.RateUnit;
import com.shegami.hr_saas.modules.mission.entity.Client;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "contracts")
public class Contract {

    @ManyToOne(fetch = FetchType.LAZY)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    private Employee employee;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Double rate;

    @Enumerated(EnumType.STRING)
    private RateUnit rateUnit;

    private String signedPdfUrl;

    @Enumerated(EnumType.STRING)
    private ContractStatus status;

    private LocalDateTime hireDate;


    @Id
    @Column(name = "contract_id", nullable = false)
    private String contractId;
    @PrePersist
    public void generateContractId() {
        if (this.contractId == null) {
            this.contractId = "CTR-" + UUID.randomUUID();
        }
    }
}
