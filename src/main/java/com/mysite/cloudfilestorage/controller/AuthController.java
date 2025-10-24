package com.mysite.cloudfilestorage.controller;

import com.mysite.cloudfilestorage.dto.AuthRequest;
import com.mysite.cloudfilestorage.dto.AuthResponse;
import com.mysite.cloudfilestorage.security.AuthService;
import com.mysite.cloudfilestorage.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody @Valid AuthRequest authRequest) {
        AuthResponse register = userService.register(authRequest);
        authService.auth(authRequest);

        return register;
    }
}
