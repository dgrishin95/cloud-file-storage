package com.mysite.cloudfilestorage.service;

import com.mysite.cloudfilestorage.dto.AuthRequest;
import com.mysite.cloudfilestorage.dto.AuthResponse;
import com.mysite.cloudfilestorage.mapper.UserMapper;
import com.mysite.cloudfilestorage.model.User;
import com.mysite.cloudfilestorage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(AuthRequest authRequest) {
        String encodedPassword = passwordEncoder.encode(authRequest.getPassword());

        User newUser = userMapper.toUser(authRequest);
        newUser.setPassword(encodedPassword);
        User savedUser = userRepository.save(newUser);

        return userMapper.toAuthResponse(savedUser);
    }
}
