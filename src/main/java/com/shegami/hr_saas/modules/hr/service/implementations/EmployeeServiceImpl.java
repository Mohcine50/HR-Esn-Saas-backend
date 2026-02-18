package com.shegami.hr_saas.modules.hr.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.entity.UserRole;
import com.shegami.hr_saas.modules.auth.entity.UserSettings;
import com.shegami.hr_saas.modules.auth.enums.UserRoles;
import com.shegami.hr_saas.modules.auth.enums.UserStatus;
import com.shegami.hr_saas.modules.auth.exception.UserAlreadyExistException;
import com.shegami.hr_saas.modules.auth.exception.UserNotFoundException;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.repository.UserSettingsRepository;
import com.shegami.hr_saas.modules.auth.service.SecurityTokenService;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.auth.service.UserRoleService;
import com.shegami.hr_saas.modules.auth.service.UserService;
import com.shegami.hr_saas.modules.hr.dto.EmployeeDto;
import com.shegami.hr_saas.modules.hr.dto.EmployeesCountByContract;
import com.shegami.hr_saas.modules.hr.dto.InvitationRequestDto;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.hr.entity.Invitation;
import com.shegami.hr_saas.modules.hr.enums.EmployeeStatus;
import com.shegami.hr_saas.modules.hr.enums.InvitationStatus;
import com.shegami.hr_saas.modules.hr.enums.InvitationType;
import com.shegami.hr_saas.modules.hr.exception.EmployeeNotFoundException;
import com.shegami.hr_saas.modules.hr.mapper.EmployeeMapper;
import com.shegami.hr_saas.modules.hr.repository.EmployeeRepository;
import com.shegami.hr_saas.modules.hr.repository.InvitationRepository;
import com.shegami.hr_saas.modules.hr.service.EmployeeService;
import com.shegami.hr_saas.modules.hr.service.InvitationService;
import com.shegami.hr_saas.modules.notifications.dto.EmailInvitationMessage;
import com.shegami.hr_saas.modules.notifications.dto.VerificationEmailEventDto;
import com.shegami.hr_saas.modules.notifications.rabbitmq.publisher.EventPublisher;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.shegami.hr_saas.modules.hr.utils.PasswordGenerator.generatePassword;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final TenantService tenantService;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;
    private final InvitationRepository invitationRepository;
    private final UserSettingsRepository settingsRepository;

    private final EventPublisher eventPublisher;

    @Value("${app.invitation.expiry-days}")
    private int invitationExpiryDays;


    @Override
    public EmployeeDto getEmployeeById(String id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(()->new EmployeeNotFoundException("Employee Not Found With Id : " + id));
        return employeeMapper.toDto(employee);
    }

    @Override
    public Employee getEmployeeByEmail(String Email) {
        return null;
    }

    @Override
    public Employee saveEmployee(Employee employee) {
        return null;
    }

    @Override
    public Employee updateEmployee(Employee employee) {
        return null;
    }

    @Override
    public void deleteEmployee(String id) {

    }


    @Override
    @Transactional
    public EmployeeDto addNewEmployee(InvitationRequestDto employee) {
        validateUserDoesNotExist(employee.getEmail());

        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        log.info("Invite employee with email {} for tenant {}", employee.getEmail(), tenantId);

        handleExistingInvitation(employee.getEmail(), tenantId);

        Tenant tenant = tenantService.getTenant(tenantId);
        User inviter = getInviter(UserContextHolder.getCurrentUserContext().userId());
        UserRole userRole = userRoleService.getUserRoleByName(UserRoles.valueOf(employee.getRoleName()));

        User newUser = createUser(employee, tenant, userRole);
        Employee newEmployee = createEmployee(employee, tenant, newUser);

        String token = TokenGenerator.generateToken();

        Invitation invitation = createInvitation(newUser, tenant, inviter, token);
        sendInvitationEmail(employee, tenant, inviter, invitation, token);

        return employeeMapper.toDto(newEmployee);
    }


    @Override
    public Page<EmployeeDto> getAllEmployees(Pageable pageable) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        String userId = UserContextHolder.getCurrentUserContext().userId();
        return employeeRepository.findByTenantId(pageable,tenantId, userId).map(employeeMapper::toDto);
    }

    @Override
    public List<EmployeesCountByContract> countEmployeesByContract() {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        return employeeRepository.countEmployeeByContractType(tenantId);
    }

    private void validateUserDoesNotExist(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistException("User with email " + email + " already exists");
        }
    }

    private void handleExistingInvitation(String email, String tenantId) {
        Optional<Invitation> existingInvitation = invitationRepository
                .findByInviteeEmailAndTenantTenantIdAndStatus(email, tenantId, InvitationStatus.PENDING);

        if (existingInvitation.isPresent()) {
            Invitation existing = existingInvitation.get();
            if (existing.getInvitedAt().plusDays(invitationExpiryDays).isAfter(LocalDateTime.now())) {
                throw new UserAlreadyExistException("Active invitation already exists for this email");
            }
            existing.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(existing);
        }
    }

    private User getInviter(String userId) {
        return userService.findUserByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("No user found with userId : " + userId));
    }

    private User createUser(InvitationRequestDto employee, Tenant tenant, UserRole userRole) {
        String password = generatePassword();
        UserSettings userSettings = settingsRepository.save(new UserSettings());

        User newUser = new User();
        newUser.setFirstName(employee.getFirstName());
        newUser.setLastName(employee.getLastName());
        newUser.setTenant(tenant);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(employee.getEmail());
        newUser.setStatus(UserStatus.INVITED);
        newUser.setPending(true);
        newUser.setIsEmailVerified(false);
        newUser.getRoles().add(userRole);
        newUser.setUserSettings(userSettings);

        return userRepository.save(newUser);
    }

    private Employee createEmployee(InvitationRequestDto employee, Tenant tenant, User savedUser) {
        Employee newEmployee = new Employee();
        newEmployee.setUser(savedUser);
        newEmployee.setPosition(employee.getPosition());
        newEmployee.setSalary(employee.getSalary());
        newEmployee.setCurrency(employee.getCurrency());
        newEmployee.setContractType(employee.getContractType());
        newEmployee.setStatus(EmployeeStatus.ACTIVE);
        newEmployee.setHireDate(LocalDateTime.now());
        newEmployee.setTenant(tenant);

        return employeeRepository.save(newEmployee);
    }

    private Invitation createInvitation(User savedUser, Tenant tenant, User inviter, String token) {
        String tokenHash = TokenGenerator.encryptToken(token);

        Invitation invitation = new Invitation();
        invitation.setInvitationToken(tokenHash);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setInvitedAt(LocalDateTime.now());
        invitation.setInviter(inviter);
        invitation.setTenant(tenant);
        invitation.setInvitationType(InvitationType.EMPLOYEE);
        invitation.setInvitee(savedUser);

        Invitation savedInvitation = invitationRepository.save(invitation);
        log.info("Invitation created with ID: {}", savedInvitation.getInvitationId());
        return savedInvitation;
    }

    private void sendInvitationEmail(InvitationRequestDto employee, Tenant tenant, User inviter, Invitation invitation, String token) {
        eventPublisher.publishInvitationEmail(
                EmailInvitationMessage.builder()
                        .companyName(tenant.getName())
                        .recipientEmail(employee.getEmail())
                        .recipientFirstName(employee.getFirstName())
                        .recipientLastName(employee.getLastName())
                        .inviterName(inviter.getFirstName())
                        .role(employee.getRoleName())
                        .invitationToken(token)
                        .build()
        );
    }




}
