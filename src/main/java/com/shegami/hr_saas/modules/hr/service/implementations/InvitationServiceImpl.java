package com.shegami.hr_saas.modules.hr.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.enums.UserRoles;
import com.shegami.hr_saas.modules.auth.enums.UserStatus;
import com.shegami.hr_saas.modules.auth.exception.UserAlreadyExistException;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.hr.dto.InvitationDto;
import com.shegami.hr_saas.modules.hr.mapper.InvitationMapper;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.hr.entity.Invitation;
import com.shegami.hr_saas.modules.hr.enums.EmployeeStatus;
import com.shegami.hr_saas.modules.hr.enums.InvitationStatus;
import com.shegami.hr_saas.modules.hr.exception.InvitationExpiredException;
import com.shegami.hr_saas.modules.hr.repository.EmployeeRepository;
import com.shegami.hr_saas.modules.hr.repository.InvitationRepository;
import com.shegami.hr_saas.modules.hr.service.InvitationService;
import com.shegami.hr_saas.modules.notifications.service.EmailSenderService;
import com.shegami.hr_saas.shared.exception.ResourceNotFoundException;
import com.shegami.hr_saas.shared.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final InvitationMapper invitationMapper;
    private final EmailSenderService emailService;
    private final TenantService tenantService;

    @Value("${app.invitation.expiry-days}")
    private int invitationExpiryDays;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public InvitationDto createInvitation(InvitationDto invitationDto) {
        log.info("Creating invitation for email: {}", invitationDto.getEnviteeEmail());

        // Get current user context
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        String currentUserId = UserContextHolder.getCurrentUserContext().userId();

        Tenant tenant = tenantService.getTenant(tenantId);

        // Validate email format
        if (!isValidEmail(invitationDto.getEnviteeEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Check if user already exists
        if (userRepository.existsByEmail(invitationDto.getEnviteeEmail())) {
            throw new UserAlreadyExistException("User with email " + invitationDto.getEnviteeEmail() + " already exists");
        }



        // Check for existing pending invitation
        Optional<Invitation> existingInvitation = invitationRepository
                .findByEnviteeEmailAndTenantTenantIdAndStatus(
                        invitationDto.getEnviteeEmail(),
                        tenantId,
                        InvitationStatus.PENDING
                );

        if (existingInvitation.isPresent()) {
            Invitation existing = existingInvitation.get();
            if (existing.getInvitedAt().plusDays(invitationExpiryDays).isAfter(LocalDateTime.now())) {
                throw new UserAlreadyExistException("Active invitation already exists for this email");
            } else {
                // Expire old invitation
                existing.setStatus(InvitationStatus.EXPIRED);
                invitationRepository.save(existing);
            }
        }

        // Get inviter
        User inviter = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        // Generate secure token
        String token = TokenGenerator.generateToken();
        String tokenHash = TokenGenerator.encryptToken(token);

        // Create invitation
        Invitation invitation = new Invitation();
        invitation.setEnviteeEmail(invitationDto.getEnviteeEmail().toLowerCase());
        invitation.setInvitationToken(tokenHash);
        invitation.setUserRole(invitationDto.getUserRole());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setInvitedAt(LocalDateTime.now());
        invitation.setInviter(inviter);
        invitation.setTenant(tenant);

        Invitation savedInvitation = invitationRepository.save(invitation);
        log.info("Invitation created with ID: {}", savedInvitation.getInvitationId());

        // Create pending user
        createPendingUser(savedInvitation, invitationDto);

        // Build invitation link
        String invitationLink = String.format("%s/accept-invitation?token=%s", baseUrl, token);

        // Send invitation email
        try {
            emailService.sendInvitationEmail(
                    invitationDto.getEnviteeEmail(),
                    invitationLink
            );
            log.info("Invitation email sent to: {}", invitationDto.getEnviteeEmail());
        } catch (Exception e) {
            log.error("Failed to send invitation email", e);
            // Don't fail the entire operation if email fails
        }

        return invitationMapper.toDto(savedInvitation);
    }

    @Override
    @Transactional(readOnly = true)
    public InvitationDto getInvitation(String invitationId) {
        log.info("Fetching invitation: {}", invitationId);

        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();

        Invitation invitation = invitationRepository
                .findByInvitationIdAndTenantTenantId(invitationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        return invitationMapper.toDto(invitation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invitation> getAllInvitations() {
        log.info("Fetching all invitations");

        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        return invitationRepository.findByTenantTenantIdOrderByInvitedAtDesc(tenantId);
    }

    @Override
    public boolean updateInvitation(String invitationId, InvitationDto invitationDto) {
        log.info("Updating invitation: {}", invitationId);

        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();

        Invitation invitation = invitationRepository
                .findByInvitationIdAndTenantTenantId(invitationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Cannot update non-pending invitation");
        }

        // Update fields
        if (invitationDto.getUserRole() != null) {
            invitation.setUserRole(invitationDto.getUserRole());
        }

        invitationRepository.save(invitation);
        log.info("Invitation updated: {}", invitationId);

        return true;
    }

    @Override
    public boolean deleteInvitation(String invitationId) {
        log.info("Deleting invitation: {}", invitationId);

        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();

        Invitation invitation = invitationRepository
                .findByInvitationIdAndTenantTenantId(invitationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        // Delete pending user if exists
        userRepository.findByEmail(invitation.getEnviteeEmail())
                .ifPresent(user -> {
                    if (user.isPending()) {
                        userRepository.delete(user);
                        log.info("Deleted pending user: {}", user.getEmail());
                    }
                });

        invitationRepository.delete(invitation);
        log.info("Invitation deleted: {}", invitationId);

        return true;
    }

    @Override
    public boolean revokeInvitation(String invitationId) {
        log.info("Revoking invitation: {}", invitationId);

        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();

        Invitation invitation = invitationRepository
                .findByInvitationIdAndTenantTenantId(invitationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Can only revoke pending invitations");
        }

        invitation.setStatus(InvitationStatus.REVOKED);
        invitationRepository.save(invitation);

        // Delete pending user if exists
        userRepository.findByEmail(invitation.getEnviteeEmail())
                .ifPresent(user -> {
                    if (user.isPending()) {
                        userRepository.delete(user);
                    }
                });

        log.info("Invitation revoked: {}", invitationId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateInvitation(String token) {
        log.info("Validating invitation token");

        try {
            String tokenHash = TokenGenerator.encryptToken(token);

            Invitation invitation = invitationRepository
                    .findByInvitationToken(tokenHash)
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation token"));

            // Check status
            if (invitation.getStatus() != InvitationStatus.PENDING) {
                log.warn("Invitation is not pending: {}", invitation.getStatus());
                return false;
            }

            // Check expiration
            LocalDateTime expiryDate = invitation.getInvitedAt().plusDays(invitationExpiryDays);
            if (LocalDateTime.now().isAfter(expiryDate)) {
                invitation.setStatus(InvitationStatus.EXPIRED);
                invitationRepository.save(invitation);
                throw new InvitationExpiredException("Invitation has expired");
            }

            log.info("Invitation validated successfully");
            return true;

        } catch (Exception e) {
            log.error("Token validation failed", e);
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    @Override
    public boolean acceptInvitation(String token) {
        log.info("Accepting invitation");

        // Validate token first
        validateInvitation(token);

        String tokenHash = TokenGenerator.encryptToken(token);
        Invitation invitation = invitationRepository
                .findByInvitationToken(tokenHash)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        // Find pending user
        User user = userRepository
                .findByEmail(invitation.getEnviteeEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Pending user not found"));

        if (!user.isPending()) {
            throw new IllegalStateException("User is not in pending state");
        }

        // Activate user (password should be set separately via /set-password endpoint)
        user.setPending(false);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setIsEmailVerified(true);
        userRepository.save(user);

        // Update invitation status
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        // Create employee record if role is EMPLOYEE
        if (invitation.getUserRole() == UserRoles.EMPLOYEE) {
            createEmployeeRecord(user);
        }

        log.info("Invitation accepted for user: {}", user.getEmail());
        return true;
    }

    @Override
    public boolean rejectInvitation(String invitationId) {
        log.info("Rejecting invitation: {}", invitationId);

        Invitation invitation = invitationRepository
                .findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Can only reject pending invitations");
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        invitationRepository.save(invitation);

        // Delete pending user if exists
        userRepository.findByEmail(invitation.getEnviteeEmail())
                .ifPresent(user -> {
                    if (user.isPending()) {
                        userRepository.delete(user);
                    }
                });

        log.info("Invitation rejected: {}", invitationId);
        return true;
    }

    // Private helper methods
    private void createPendingUser(Invitation invitation, InvitationDto invitationDto) {
        User pendingUser = new User();
        pendingUser.setEmail(invitation.getEnviteeEmail());
        pendingUser.setFirstName(invitationDto.getFirstName());
        pendingUser.setLastName(invitationDto.getLastName());
        pendingUser.setPending(true);
        pendingUser.setStatus(UserStatus.INVITED);
        pendingUser.setIsEmailVerified(false);
        pendingUser.setCreatedFromInvitation(invitation);
        pendingUser.setTenant(invitation.getTenant());

        userRepository.save(pendingUser);
        log.info("Created pending user for: {}", invitation.getEnviteeEmail());
    }

    private void createEmployeeRecord(User user) {
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setHireDate(LocalDateTime.now());
        employee.setTenant(user.getTenant());

        employeeRepository.save(employee);
        log.info("Created employee record for user: {}", user.getEmail());
    }


    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}