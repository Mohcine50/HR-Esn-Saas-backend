package com.shegami.hr_saas.modules.mission.entity;

import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "mission_comments")
public class MissionComment extends BaseTenantEntity {

    @Id
    @Column(name = "comment_id", nullable = false)
    private String commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String authorId;

    @Column(nullable = false)
    private String authorName;

    @Column(nullable = false)
    private boolean edited = false;

    @PrePersist
    public void generateId() {
        if (this.commentId == null) {
            this.commentId = "CMT-" + UUID.randomUUID();
        }
    }
}