package com.mysite.cloudfilestorage.controller;

import com.mysite.cloudfilestorage.dto.UserResponse;
import com.mysite.cloudfilestorage.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getInfo() {
        return userService.getInfo();
    }
}
