package com.shegami.hr_saas.modules.hr.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.entity.UserRole;
import com.shegami.hr_saas.modules.auth.enums.UserRoles;
import com.shegami.hr_saas.modules.hr.enums.InvitationStatus;
import com.shegami.hr_saas.modules.hr.enums.InvitationType;
import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "invitations")
@Getter
@Setter
public class Invitation extends BaseTenantEntity {
    @Id
    private String invitationId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="user_id")
    private User invitee;

    private String invitationToken;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    private LocalDateTime invitedAt;

    private LocalDateTime acceptedAt;

     @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "inviter_id")
     private User inviter;


     @Enumerated(EnumType.STRING)
     private InvitationType invitationType;


    @Enumerated(EnumType.STRING)
    private UserRoles userRole;


    @PrePersist
    public void generateInvitationId() {
        if (this.invitationId == null) {
            this.invitationId = "INV-" + UUID.randomUUID();
        }
    }


}
