package com.mysite.cloudfilestorage.service;

import com.mysite.cloudfilestorage.dto.UserSignUpRequest;
import com.mysite.cloudfilestorage.dto.UserSignUpResponse;
import com.mysite.cloudfilestorage.model.User;
import com.mysite.cloudfilestorage.repository.UserRepository;
import com.mysite.cloudfilestorage.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserSignUpResponse register(UserSignUpRequest userSignUpRequest) {
        return new UserSignUpResponse(userSignUpRequest.getUsername());
    }

    @Override
    public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new UserPrincipal(user);
    }
}
