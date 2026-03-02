package com.shegami.hr_saas.modules.auth.service.implemtation;


import com.shegami.hr_saas.modules.auth.dto.UserDto;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.entity.UserRole;
import com.shegami.hr_saas.modules.auth.entity.UserSettings;
import com.shegami.hr_saas.modules.auth.enums.UserRoles;
import com.shegami.hr_saas.modules.auth.enums.UserStatus;
import com.shegami.hr_saas.modules.auth.exception.UserAlreadyExistException;
import com.shegami.hr_saas.modules.auth.mapper.UserMapper;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.repository.UserSettingsRepository;
import com.shegami.hr_saas.modules.auth.service.UserRoleService;
import com.shegami.hr_saas.modules.auth.service.UserService;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.hr.enums.EmployeeStatus;
import com.shegami.hr_saas.modules.hr.repository.EmployeeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;

    private final UserRoleService userRoleService;

    private final UserSettingsRepository settingsRepository;

    private final UserMapper userMapper;
    private final EmployeeRepository employeeRepository;

    @Override
    public Optional<User> findUserByEmail(String email) {

        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findUserByUserId(String userId) {
        return userRepository.findUsersByUserId(userId);
    }

    @Transactional
    @Override
    public User createUser(UserDto userDto, Tenant tenant) {

        userRepository.findByEmail(userDto.getEmail()).ifPresent(u -> {
            throw new UserAlreadyExistException("User already exists, please try another email.");
        });

        UserRole userRole = userRoleService.getUserRoleByName(UserRoles.COMPANY_OWNER);
        UserSettings userSettings = settingsRepository.save(new UserSettings());

        User newUser = new User();
        newUser.setEmail(userDto.getEmail());
        newUser.setPassword(userDto.getPassword());
        newUser.setFirstName(userDto.getFirstName());
        newUser.setLastName(userDto.getLastName());
        newUser.setTenant(tenant);
        newUser.setPhoneNumber(userDto.getPhoneNumber());
        newUser.setStatus(UserStatus.PENDING);
        newUser.setUserSettings(userSettings);
        newUser.setRoles(new ArrayList<>(List.of(userRole)));

        User createdUser = userRepository.save(newUser);

        Employee employee = new Employee();
        employee.setTenant(tenant);
        employee.setUser(createdUser);
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setHireDate(LocalDateTime.now());
        var savedEmployee = employeeRepository.save(employee);
        createdUser.setEmployee(savedEmployee);

        return createdUser;
    }



    @Override
    public void updateUser(User appUser) {
        userRepository.save(appUser);
    }


}
