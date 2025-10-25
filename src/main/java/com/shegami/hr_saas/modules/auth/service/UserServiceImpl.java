package com.shegami.hr_saas.modules.auth.service;


import com.shegami.hr_saas.modules.auth.dto.RegisterDto;
import com.shegami.hr_saas.modules.auth.dto.UserDto;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.mapper.UserMapper;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.shared.exception.ApiRequestException;
import com.shegami.hr_saas.shared.exception.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Override
    public User findUserByEmail(String email) {

        return userRepository.findUserByEmail(email).orElseThrow(()->new UserNotFoundException("User not found"));
    }

    @Override
    public boolean createUser(RegisterDto registerDto) {
        User user = userRepository.findUserByEmail(registerDto.getEmail()).orElseThrow(()->
                new UserNotFoundException("User Not found exception"));

        if (user != null) {
            throw new ApiRequestException("User Already Exist please try other one");
        }

        User newUser = userMapper.toEntity(UserDto.builder().build());


        var createdUser = userRepository.save(newUser);


        return createdUser != null;
    }

    @Override
    public void updateUser(User appUser) {
    userRepository.save(appUser);
    }


}
