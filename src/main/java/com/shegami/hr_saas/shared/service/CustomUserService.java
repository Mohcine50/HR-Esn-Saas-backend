package com.shegami.hr_saas.shared.service;

import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        com.shegami.hr_saas.modules.auth.entity.User userResponse = userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException(email));

        return User
                .withUsername(userResponse.getEmail())
                .password(userResponse.getPassword())
                .build();
    }



}
