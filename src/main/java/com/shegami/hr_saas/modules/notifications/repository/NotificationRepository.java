package com.shegami.hr_saas.modules.notifications.repository;

import com.shegami.hr_saas.modules.notifications.entity.Notification;
import com.shegami.hr_saas.modules.notifications.enums.NotificationStatus;
import com.shegami.hr_saas.modules.notifications.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
  /**
   * Find notification by ID and user
   */
  Optional<Notification> findByNotificationIdAndRecipient_UserId(
          String notificationId,
          String userId
  );

  /**
   * Find all notifications for a user
   */
  Page<Notification> findByRecipient_UserIdAndTenantTenantIdOrderByCreatedAtDesc(
          String userId,
          String tenantId,
          Pageable pageable
  );

  /**
   * Find unread notifications for a user
   */
  Page<Notification> findByRecipient_UserIdAndStatusOrderByCreatedAtDesc(
          String userId,
          NotificationStatus status,
          Pageable pageable
  );

  /**
   * Find notifications by type
   */
  Page<Notification> findByRecipient_UserIdAndNotificationTypeOrderByCreatedAtDesc(
          String recipient_userId, NotificationType notificationType, Pageable pageable
  );

  /**
   * Count unread notifications for a user
   */
  @Query("SELECT COUNT(n) FROM Notification n " +
          "WHERE n.recipient.userId = :userId " +
          "AND n.status = 'UNREAD'")
  Long countUnreadByUserId(@Param("userId") String userId);

  /**
   * Count all notifications for a user
   */
  Long countByRecipient_UserIdAndTenantTenantId(String userId, String tenantId);

  /**
   * Count by status
   */
  Long countByRecipient_UserIdAndStatus(String userId, NotificationStatus status);

  // ==================== BULK OPERATIONS ====================

  /**
   * Mark all as read for a user
   */
  @Modifying
  @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :readAt " +
          "WHERE n.recipient.userId = :userId AND n.status = 'UNREAD'")
  int markAllAsReadForUser(
          @Param("userId") String userId,
          @Param("readAt") LocalDateTime readAt
  );

  /**
   * Mark specific notifications as read
   */
  @Modifying
  @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :readAt " +
          "WHERE n.notificationId IN :notificationIds " +
          "AND n.recipient.userId = :userId")
  int markAsRead(
          @Param("notificationIds") List<String> notificationIds,
          @Param("userId") String userId,
          @Param("readAt") LocalDateTime readAt
  );

  /**
   * Find notifications related to a specific entity
   */
  @Query("SELECT n FROM Notification n " +
          "WHERE n.entityType = :entityType " +
          "AND n.entityId = :entityId " +
          "ORDER BY n.createdAt DESC")
  List<Notification> findByEntity(
          @Param("entityType") String entityType,
          @Param("entityId") String entityId
  );

  /**
   * Find notifications for user about specific entity
   */
  @Query("SELECT n FROM Notification n " +
          "WHERE n.recipient.userId = :userId " +
          "AND n.entityType = :entityType " +
          "AND n.entityId = :entityId " +
          "ORDER BY n.createdAt DESC")
  List<Notification> findByUserAndEntity(
          @Param("userId") String userId,
          @Param("entityType") String entityType,
          @Param("entityId") String entityId
  );

  /**
   * Get recent unread notifications for a user
   */
  @Query("SELECT n FROM Notification n " +
          "WHERE n.recipient.userId = :userId " +
          "AND n.status = 'UNREAD' " +
          "AND n.createdAt > :since " +
          "ORDER BY n.createdAt DESC")
  List<Notification> findRecentUnread(
          @Param("userId") String userId,
          @Param("since") LocalDateTime since
  );

  /**
   * Get latest notifications for a user
   */
  @Query("SELECT n FROM Notification n " +
          "WHERE n.recipient.userId = :userId " +
          "AND n.status != 'DELETED' " +
          "ORDER BY n.createdAt DESC")
  Page<Notification> findLatestForUser(
          @Param("userId") String userId,
          Pageable pageable
  );
}