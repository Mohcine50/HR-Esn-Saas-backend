package com.shegami.hr_saas.modules.auth.service.implemtation;


import com.shegami.hr_saas.modules.auth.dto.RegisterDto;
import com.shegami.hr_saas.modules.auth.dto.UserDto;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.entity.UserRole;
import com.shegami.hr_saas.modules.auth.enums.UserRoles;
import com.shegami.hr_saas.modules.auth.enums.UserStatus;
import com.shegami.hr_saas.modules.auth.exception.UserAlreadyExistException;
import com.shegami.hr_saas.modules.auth.mapper.UserMapper;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.service.UserRoleService;
import com.shegami.hr_saas.modules.auth.service.UserService;
import com.shegami.hr_saas.shared.exception.ApiRequestException;
import com.shegami.hr_saas.modules.auth.exception.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;

    private final UserRoleService userRoleService;


    private final UserMapper userMapper;

    @Override
    public Optional<User> findUserByEmail(String email) {

        return userRepository.findByEmail(email);
    }

    @Override
    public boolean createUser(UserDto userDto, Tenant tenant) {

        userRepository.findByEmail(userDto.getEmail()).ifPresent(u -> {
            throw new UserAlreadyExistException("User already exists, please try another email.");
        });

        UserRole userRole = userRoleService.getUserRoleByName(UserRoles.ADMIN.toString());



        User newUser = new User();
        newUser.setEmail(userDto.getEmail());
        newUser.setPassword(userDto.getPassword());
        newUser.setFirstName(userDto.getFirstName());
        newUser.setLastName(userDto.getLastName());
        newUser.setRoles(new ArrayList<>());
        newUser.setTenant(tenant);
        newUser.setPhoneNumber(userDto.getPhoneNumber());
        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setRoles(new ArrayList<>(List.of(userRole)));

        var createdUser = userRepository.save(newUser);

        return createdUser != null;
    }



    @Override
    public void updateUser(User appUser) {
        userRepository.save(appUser);
    }


}
