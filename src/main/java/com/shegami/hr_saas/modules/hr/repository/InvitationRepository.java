package com.shegami.hr_saas.modules.hr.repository;

import com.shegami.hr_saas.modules.hr.entity.Invitation;
import com.shegami.hr_saas.modules.hr.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, String> {
    // Find by token hash
    Optional<Invitation> findByInvitationToken(String invitationToken);

    // Find by email and tenant
    Optional<Invitation> findByInviteeEmailAndTenantTenantIdAndStatus(
            String email,
            String tenantId,
            InvitationStatus status
    );

    // Find by invitation ID and tenant
    Optional<Invitation> findByInvitationIdAndTenantTenantId(String invitationId, String tenantId);

    // Get all invitations for a tenant
    List<Invitation> findByTenantTenantIdOrderByInvitedAtDesc(String tenantId);

    // Get invitations by status
    List<Invitation> findByTenantTenantIdAndStatus(String tenantId, InvitationStatus status);

    // Get invitations created by a specific user
    @Query("SELECT i FROM Invitation i WHERE i.inviter.userId = :inviterId AND i.tenant.tenantId = :tenantId")
    List<Invitation> findByInviterIdAndTenantId(
            @Param("inviterId") String inviterId,
            @Param("tenantId") String tenantId
    );

    // Count pending invitations
    Long countByTenantTenantIdAndStatus(String tenantId, InvitationStatus status);

    // Find expired invitations
    @Query("SELECT i FROM Invitation i " +
            "WHERE i.status = 'PENDING' " +
            "AND i.invitedAt < :expiryDate")
    List<Invitation> findExpiredInvitations(@Param("expiryDate") LocalDateTime expiryDate);

    // Check if email has pending invitation
    boolean existsByInviteeEmailAndTenantTenantIdAndStatus(
            String email,
            String tenantId,
            InvitationStatus status
    );
}