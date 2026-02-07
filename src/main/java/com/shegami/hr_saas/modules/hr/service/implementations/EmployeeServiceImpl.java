package com.shegami.hr_saas.modules.hr.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.entity.UserRole;
import com.shegami.hr_saas.modules.auth.enums.UserStatus;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.auth.service.UserRoleService;
import com.shegami.hr_saas.modules.auth.service.UserService;
import com.shegami.hr_saas.modules.hr.dto.EmployeeDto;
import com.shegami.hr_saas.modules.hr.dto.EmployeesCountByContract;
import com.shegami.hr_saas.modules.hr.dto.InviteEmployeeDto;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.hr.enums.EmployeeStatus;
import com.shegami.hr_saas.modules.hr.exception.EmployeeNotFoundException;
import com.shegami.hr_saas.modules.hr.mapper.EmployeeMapper;
import com.shegami.hr_saas.modules.hr.repository.EmployeeRepository;
import com.shegami.hr_saas.modules.hr.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    public EmployeeDto AddNewEmployee(InviteEmployeeDto employee) {

        // Get Tenant from db
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        Tenant tenant = tenantService.getTenant(tenantId);

        //TODO: needs to make it case insensitive
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
        newEmployee.setStatus(EmployeeStatus.ACTIVE);
        newEmployee.setHireDate(LocalDateTime.now());
        newEmployee.setTenant(tenant);

        // Save employee
        Employee savedEmployee = employeeRepository.save(newEmployee);

        // Optional: Send invitation email
        // sendInvitationEmail(savedUser);

        return employeeMapper.toDto(savedEmployee);
    }

    @Override
    public Page<EmployeeDto> getAllEmployees(Pageable pageable) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        return employeeRepository.findByTenantId(pageable,tenantId).map(employeeMapper::toDto);
    }

    @Override
    public List<EmployeesCountByContract> countEmployeesByContract() {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        return employeeRepository.countEmployeeByContractType(tenantId);
    }
}
