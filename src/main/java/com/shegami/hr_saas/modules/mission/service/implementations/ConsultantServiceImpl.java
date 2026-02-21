package com.shegami.hr_saas.modules.mission.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.entity.UserRole;
import com.shegami.hr_saas.modules.auth.entity.UserSettings;
import com.shegami.hr_saas.modules.auth.enums.UserRoles;
import com.shegami.hr_saas.modules.auth.enums.UserStatus;
import com.shegami.hr_saas.modules.auth.exception.UserAlreadyExistException;
import com.shegami.hr_saas.modules.auth.exception.UserNotFoundException;
import com.shegami.hr_saas.modules.auth.mapper.UserMapper;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.repository.UserSettingsRepository;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.auth.service.UserRoleService;
import com.shegami.hr_saas.modules.hr.entity.Invitation;
import com.shegami.hr_saas.modules.hr.enums.InvitationStatus;
import com.shegami.hr_saas.modules.hr.enums.InvitationType;
import com.shegami.hr_saas.modules.hr.repository.InvitationRepository;
import com.shegami.hr_saas.modules.mission.dto.ConsultantDto;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.mission.enums.ConsultantStatus;
import com.shegami.hr_saas.modules.mission.mapper.ConsultantMapper;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.mission.service.ConsultantService;
import com.shegami.hr_saas.modules.notifications.dto.EmailInvitationMessage;
import com.shegami.hr_saas.modules.notifications.rabbitmq.publisher.EventPublisher;
import com.shegami.hr_saas.shared.exception.AlreadyExistsException;
import com.shegami.hr_saas.shared.exception.ResourceNotFoundException;
import com.shegami.hr_saas.shared.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.shegami.hr_saas.modules.hr.utils.PasswordGenerator.generatePassword;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultantServiceImpl implements ConsultantService {

    private final ConsultantRepository consultantRepository;
    private final ConsultantMapper consultantMapper;
    private final TenantService tenantService;
    private final UserRoleService userRoleService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSettingsRepository userSettingsRepository;
    private final InvitationRepository invitationRepository;
    private final EventPublisher eventPublisher;


    @Value("${app.invitation.expiry-days}")
    private int invitationExpiryDays;

    @Override
    @Transactional(readOnly = true)
    public ConsultantDto getConsultantById(String id) {
        log.debug("Fetching consultant by ID: {}", id);
        return consultantRepository.findById(id)
                .map(consultantMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultantDto getConsultantByEmail(String email) {
        log.debug("Fetching consultant by email: {}", email);
        return consultantRepository.findByEmail(email)
                .map(consultantMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant not found with email: " + email));
    }

    @Override
    @Transactional
    public ConsultantDto saveConsultant(ConsultantDto consultantDto) {
        log.info("Saving new consultant: {}", consultantDto.getEmail());

        if (consultantRepository.existsByEmail(consultantDto.getEmail())) {
            throw new AlreadyExistsException("Email already in use: " + consultantDto.getEmail());
        }
        var tenantId = UserContextHolder.getCurrentUserContext().tenantId();

        Optional<Invitation> existingInvitation = invitationRepository
                .findByInviteeEmailAndTenantTenantIdAndStatus(consultantDto.getEmail(), tenantId, InvitationStatus.PENDING);

        if (existingInvitation.isPresent()) {
            Invitation existing = existingInvitation.get();
            if (existing.getInvitedAt().plusDays(invitationExpiryDays).isAfter(LocalDateTime.now())) {
                throw new UserAlreadyExistException("Active invitation already exists for this email");
            }
            existing.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(existing);
        }

        // Get Tenant from db
        Tenant tenant = tenantService.getTenant(tenantId);

        String userId = UserContextHolder.getCurrentUserContext().userId();

        User inviter = userRepository.findUsersByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("No user found with userId : " + userId));


        UserRole userRole = userRoleService.getUserRoleByName(UserRoles.CONSULTANT);
        UserSettings settings = userSettingsRepository.save(new UserSettings());

        // Create new User
        String password = generatePassword();
        User newUser = new User();
        newUser.setFirstName(consultantDto.getFirstName());
        newUser.setLastName(consultantDto.getLastName());
        newUser.setTenant(tenant);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(consultantDto.getEmail());
        newUser.setStatus(UserStatus.INVITED);
        newUser.setPending(true);
        newUser.setUserSettings(settings);
        newUser.setIsEmailVerified(false);
        newUser.getRoles().add(userRole);

        // Save user first
        User savedUser = userRepository.save(newUser);

        // Create new Employee entity
        Consultant consultant = consultantMapper.toEntity(consultantDto);
        consultant.setUser(savedUser);
        consultant.setStatus(ConsultantStatus.AVAILABLE);
        consultant.setTenant(tenant);

        String token = TokenGenerator.generateToken();
        String tokenHash = TokenGenerator.encryptToken(token);

        Invitation invitation = new Invitation();
        invitation.setInvitationToken(tokenHash);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setInvitedAt(LocalDateTime.now());
        invitation.setInviter(inviter);
        invitation.setTenant(tenant);
        invitation.setInvitationType(InvitationType.EMPLOYEE);
        invitation.setInvitee(savedUser);

        invitationRepository.save(invitation);

        eventPublisher.publishInvitationEmail(
                EmailInvitationMessage.builder()
                        .companyName(tenant.getName())
                        .recipientEmail(consultant.getEmail())
                        .recipientFirstName(consultant.getFirstName())
                        .recipientLastName(consultant.getLastName())
                        .inviterName(inviter.getFirstName())
                        .invitationToken(token)
                        .build()
        );

        Consultant saved = consultantRepository.save(consultant);
        return consultantMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ConsultantDto updateConsultant(ConsultantDto consultantDto) {
        log.info("Updating consultant with ID: {}", consultantDto.getConsultantId());

        // Senior Tip: Check existence before saving to avoid accidental creation
        if (!consultantRepository.existsById(consultantDto.getConsultantId())) {
            throw new ResourceNotFoundException("Cannot update. Consultant not found.");
        }

        Consultant consultant = consultantMapper.toEntity(consultantDto);

        Consultant updated = consultantRepository.save(consultant);
        return consultantMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteConsultant(String id) {
        log.warn("Deleting consultant with ID: {}", id);

        if (!consultantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Consultant not found with ID: " + id);
        }

        // TODO: WE SHOULD USE A SOFT DELETE BY ADDING NEW COLUMN (DELETED=true)
        // Here we use the standard repository delete
        consultantRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsultantDto> getAllConsultant(Pageable pageable) {
        log.debug("Fetching paged consultants: Page {}, Size {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return consultantRepository.findAll(pageable)
                .map(consultantMapper::toDto);
    }

    @Override
    public Set<Consultant> getAllConsultants(Set<String> ids) {
        return new HashSet<>(consultantRepository.findAllById(ids));
    }

}
