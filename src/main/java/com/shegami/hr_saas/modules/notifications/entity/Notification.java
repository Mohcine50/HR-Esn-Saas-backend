package com.shegami.hr_saas.modules.notifications.entity;

import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.notifications.enums.EntityType;
import com.shegami.hr_saas.modules.notifications.enums.NotificationType;
import com.shegami.hr_saas.modules.notifications.enums.NotificationStatus;
import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseTenantEntity {

    @Id
    @Column(name = "notification_id", nullable = false)
    private String notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    /**
     * Notification title (short summary)
     * Example: "You've been assigned to a mission"
     */
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;


    @Column(name = "entity_type")
    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    @Column(name = "entity_id")
    private String entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(name = "actor_name")
    private String actorName;

    @Column(name = "actor_avatar_url")
    private String actorAvatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @lombok.Builder.Default
    private NotificationStatus status = NotificationStatus.UNREAD;


    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    @Column(name = "sent_in_app")
    @Builder.Default
    private Boolean sentInApp = true;

    @Column(name = "sent_via_email")
    @Builder.Default
    private Boolean sentViaEmail = false;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "action_url")
    private String actionUrl;

    @PrePersist
    public void generateNotificationId() {
        if (this.notificationId == null) {
            this.notificationId = "NTF-" + UUID.randomUUID().toString();
        }
    }
}
