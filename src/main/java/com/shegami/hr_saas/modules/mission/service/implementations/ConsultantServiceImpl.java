package com.shegami.hr_saas.modules.mission.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.entity.UserRole;
import com.shegami.hr_saas.modules.auth.enums.UserRoles;
import com.shegami.hr_saas.modules.auth.enums.UserStatus;
import com.shegami.hr_saas.modules.auth.mapper.UserMapper;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.auth.service.UserRoleService;
import com.shegami.hr_saas.modules.mission.dto.ConsultantDto;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.mission.enums.ConsultantStatus;
import com.shegami.hr_saas.modules.mission.mapper.ConsultantMapper;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.mission.service.ConsultantService;
import com.shegami.hr_saas.shared.exception.AlreadyExistsException;
import com.shegami.hr_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
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

        // Get Tenant from db
        var tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        Tenant tenant = tenantService.getTenant(tenantId);

        UserRole userRole = userRoleService.getUserRoleByName(UserRoles.EMPLOYEE.toString());

        // Create new User
        String password = generatePassword();
        User newUser = new User();
        newUser.setFirstName(consultantDto.getFirstName());
        newUser.setLastName(consultantDto.getLastName());
        newUser.setTenant(tenant);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(consultantDto.getEmail());
        newUser.setStatus(UserStatus.INVITED);
        newUser.setIsVerified(false);
        newUser.getRoles().add(userRole);

        // Save user first
        User savedUser = userRepository.save(newUser);

        // Create new Employee entity
        Consultant consultant = consultantMapper.toEntity(consultantDto);
        consultant.setUser(savedUser);
        consultant.setStatus(ConsultantStatus.AVAILABLE);
        consultant.setTenant(tenant);


        // sendInvitationEmail(savedUser);

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
