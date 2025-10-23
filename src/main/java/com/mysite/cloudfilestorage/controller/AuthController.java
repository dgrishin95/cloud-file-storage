package com.mysite.cloudfilestorage.controller;

import com.mysite.cloudfilestorage.dto.UserSignUpRequest;
import com.mysite.cloudfilestorage.dto.UserSignUpResponse;
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

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public UserSignUpResponse register(@RequestBody @Valid UserSignUpRequest userSignUpRequest) {
        return userService.register(userSignUpRequest);
    }
}
