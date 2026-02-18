package com.shegami.hr_saas.modules.hr.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.enums.UserRoles;
import com.shegami.hr_saas.modules.auth.enums.UserStatus;
import com.shegami.hr_saas.modules.auth.exception.UserAlreadyExistException;
import com.shegami.hr_saas.modules.auth.exception.UserNotFoundException;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.hr.dto.AcceptInvitationDto;
import com.shegami.hr_saas.modules.hr.dto.InvitationDto;
import com.shegami.hr_saas.modules.hr.dto.InvitationRequestDto;
import com.shegami.hr_saas.modules.hr.dto.InvitationValidationResponse;
import com.shegami.hr_saas.modules.hr.enums.InvitationType;
import com.shegami.hr_saas.modules.hr.exception.*;
import com.shegami.hr_saas.modules.hr.mapper.InvitationMapper;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.hr.entity.Invitation;
import com.shegami.hr_saas.modules.hr.enums.EmployeeStatus;
import com.shegami.hr_saas.modules.hr.enums.InvitationStatus;
import com.shegami.hr_saas.modules.hr.repository.EmployeeRepository;
import com.shegami.hr_saas.modules.hr.repository.InvitationRepository;
import com.shegami.hr_saas.modules.hr.service.InvitationService;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.notifications.service.EmailSenderService;
import com.shegami.hr_saas.shared.exception.ResourceNotFoundException;
import com.shegami.hr_saas.shared.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final PasswordEncoder passwordEncoder;

    @Value("${app.invitation.expiry-days}")
    private int invitationExpiryDays;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public InvitationDto createInvitation(InvitationRequestDto invitationDto) {
        log.info("Creating invitation for email: {}", invitationDto.getEmail());

        // Get current user context
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        String currentUserId = UserContextHolder.getCurrentUserContext().userId();

        Tenant tenant = tenantService.getTenant(tenantId);

        // Validate email format
        if (!isValidEmail(invitationDto.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Check if user already exists
        if (userRepository.existsByEmail(invitationDto.getEmail())) {
            throw new UserAlreadyExistException("User with email " + invitationDto.getEmail() + " already exists");
        }



        // Check for existing pending invitation
        Optional<Invitation> existingInvitation = invitationRepository
                .findByInviteeEmailAndTenantTenantIdAndStatus(
                        invitationDto.getEmail(),
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
        invitation.setInvitationToken(tokenHash);
        invitation.setUserRole(UserRoles.valueOf(invitationDto.getRoleName()));
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setInvitedAt(LocalDateTime.now());
        invitation.setInviter(inviter);
        invitation.setTenant(tenant);
        invitation.setInvitationType(InvitationType.EMPLOYEE);

        Invitation savedInvitation = invitationRepository.save(invitation);
        log.info("Invitation created with ID: {}", savedInvitation.getInvitationId());

        // Create pending user
        User invitee = createPendingUser(savedInvitation, invitationDto);
        invitation.setInvitee(invitee);


        // Build invitation link
        String invitationLink = String.format("%s/accept-invitation?token=%s", baseUrl, token);

        // Send invitation email
        try {
            emailService.sendInvitationEmail(
                    invitationDto.getEmail(),
                    invitationLink
            );
            log.info("Invitation email sent to: {}", invitationDto.getEmail());
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
    public void deleteInvitation(String invitationId) {
        log.info("Deleting invitation: {}", invitationId);

        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();

        Invitation invitation = invitationRepository
                .findByInvitationIdAndTenantTenantId(invitationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        // Delete pending user if exists
        userRepository.findByEmail(invitation.getInvitee().getEmail())
                .ifPresent(user -> {
                    if (user.isPending()) {
                        userRepository.delete(user);
                        log.info("Deleted pending user: {}", user.getEmail());
                    }
                });

        invitationRepository.delete(invitation);
        log.info("Invitation deleted: {}", invitationId);

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
        userRepository.findByEmail(invitation.getInvitee().getEmail())
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
    public InvitationValidationResponse validateInvitation(String token) {
        log.info("Validating invitation token");

        try {
            String tokenHash = TokenGenerator.encryptToken(token);

            Invitation invitation = invitationRepository
                    .findByInvitationToken(tokenHash)
                    .orElseThrow(() -> new InvitationNotFoundException("Invalid invitation token"));


            switch (invitation.getStatus()){
                case EXPIRED -> throw new InvitationExpiredException("Invitation has expired");
                case ACCEPTED -> throw new InvitationAlreadyAccepted("Invitation already accepted");
                case REVOKED, REJECTED -> throw new InvalidInvitationException("Invalid invitation status");
            }

            // Check expiration
            LocalDateTime expiryDate = invitation.getInvitedAt().plusDays(invitationExpiryDays);
            if (LocalDateTime.now().isAfter(expiryDate)) {
                invitation.setStatus(InvitationStatus.EXPIRED);
                invitationRepository.save(invitation);
                throw new InvitationExpiredException("Invitation has expired");
            }

            log.info("Invitation validated successfully");
            return InvitationValidationResponse.builder()
                    .firstName(invitation.getInvitee().getFirstName())
                    .tenantName(invitation.getTenant().getName())
                    .build();

        } catch (Exception e) {
            log.error("Token validation failed", e);
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    @Override
    public boolean acceptInvitation(String token, AcceptInvitationDto acceptInvitationDto) {
        log.info("Accepting invitation");

        if (!acceptInvitationDto.getPassword().equals(acceptInvitationDto.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        // Validate token first
        validateInvitation(token);

        String tokenHash = TokenGenerator.encryptToken(token);
        Invitation invitation = invitationRepository
                .findByInvitationToken(tokenHash)
                .orElseThrow(() -> new InvitationNotFoundException("Invitation not found"));

        // Find pending user
        User user = userRepository
                .findByEmail(invitation.getInvitee().getEmail())
                .orElseThrow(() -> new UserNotFoundException("Pending user not found"));

        if (!user.isPending()) {
            throw new IllegalStateException("User is not in pending state");
        }

        user.setPending(false);
        user.setPassword(passwordEncoder.encode(acceptInvitationDto.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setIsEmailVerified(true);
        userRepository.save(user);

        // Update invitation status
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        if (invitation.getInvitationType() == InvitationType.EMPLOYEE){
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
        userRepository.findByEmail(invitation.getInvitee().getEmail())
                .ifPresent(user -> {
                    if (user.isPending()) {
                        userRepository.delete(user);
                    }
                });

        log.info("Invitation rejected: {}", invitationId);
        return true;
    }

    // Private helper methods
    private User createPendingUser(Invitation invitation, InvitationRequestDto invitationDto) {
        User pendingUser = new User();
        pendingUser.setEmail(invitationDto.getEmail());
        pendingUser.setFirstName(invitationDto.getFirstName());
        pendingUser.setLastName(invitationDto.getLastName());
        pendingUser.setPending(true);
        pendingUser.setStatus(UserStatus.INVITED);
        pendingUser.setIsEmailVerified(false);
        pendingUser.setCreatedFromInvitation(invitation);
        pendingUser.setTenant(invitation.getTenant());


        log.info("Created pending user for: {}", invitationDto.getEmail());
        return userRepository.save(pendingUser);
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