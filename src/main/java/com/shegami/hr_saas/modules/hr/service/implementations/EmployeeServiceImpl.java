package com.shegami.hr_saas.modules.hr.service.implementations;

import com.shegami.hr_saas.config.domain.context.TenantContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.entity.UserRole;
import com.shegami.hr_saas.modules.auth.enums.UserStatus;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.auth.service.UserRoleService;
import com.shegami.hr_saas.modules.auth.service.UserService;
import com.shegami.hr_saas.modules.hr.dto.InviteEmployeeDto;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.hr.enums.EmployeeStatus;
import com.shegami.hr_saas.modules.hr.repository.EmployeeRepository;
import com.shegami.hr_saas.modules.hr.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.shegami.hr_saas.modules.hr.utils.PasswordGenerator.generatePassword;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final TenantService tenantService;
    private final PasswordEncoder passwordEncoder;


    @Override
    public Employee getEmployeeById(String id) {
        return null;
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
    public String AddNewEmployee(InviteEmployeeDto employee) {

        // Get Tenant from db
        String tenantId = TenantContextHolder.getCurrentTenant();
        Tenant tenant = tenantService.getTenant(tenantId);


        // Get role from db
        UserRole userRole = userRoleService.getUserRoleByName(employee.getRoleName());

        // Create new User
        User newUser = new User();

        String password = generatePassword();

        newUser.setFirstName(employee.getFirstName());
        newUser.setLastName(employee.getLastName());
        newUser.setTenant(tenant);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(employee.getEmail());
        newUser.setStatus(UserStatus.INVITED);
        newUser.setIsVerified(false);
        newUser.getRoles().add(userRole);

        // Save user first
        User savedUser = userRepository.save(newUser);

        // Create new Employee entity
        Employee newEmployee = new Employee();
        newEmployee.setUser(savedUser);
        newEmployee.setPosition(employee.getPosition());
        newEmployee.setSalary(employee.getSalary());
        newEmployee.setCurrency(employee.getCurrency());
        newEmployee.setContractType(employee.getContractType());
        newEmployee.setStatus(EmployeeStatus.ACTIVE); // Adjust based on your enum values
        newEmployee.setHireDate(LocalDateTime.now());

        // Save employee
        Employee savedEmployee = employeeRepository.save(newEmployee);

        // Optional: Send invitation email
        // sendInvitationEmail(savedUser);

        return password;
    }
}
