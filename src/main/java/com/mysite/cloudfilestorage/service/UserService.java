package com.mysite.cloudfilestorage.service;

import com.mysite.cloudfilestorage.dto.AuthRequest;
import com.mysite.cloudfilestorage.dto.AuthResponse;
import com.mysite.cloudfilestorage.dto.UserResponse;
import com.mysite.cloudfilestorage.exception.UserAlreadyExistsException;
import com.mysite.cloudfilestorage.mapper.UserMapper;
import com.mysite.cloudfilestorage.model.User;
import com.mysite.cloudfilestorage.repository.UserRepository;
import com.mysite.cloudfilestorage.security.CurrentUserProvider;
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
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public AuthResponse register(AuthRequest authRequest) {
        if (isUserExist(authRequest.getUsername())) {
            throw new UserAlreadyExistsException("Username is busy");
        }

        String encodedPassword = passwordEncoder.encode(authRequest.getPassword());

        User newUser = userMapper.toUser(authRequest);
        newUser.setPassword(encodedPassword);
        User savedUser = userRepository.save(newUser);

        return userMapper.toAuthResponse(savedUser);
    }

    private boolean isUserExist(String username) {
        return userRepository.existsByUsername(username);
    }

    public UserResponse getInfo() {
        return new UserResponse(currentUserProvider.getCurrentUser().getUsername());
    }
}
