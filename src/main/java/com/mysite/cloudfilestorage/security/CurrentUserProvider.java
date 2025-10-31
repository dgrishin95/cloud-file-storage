package com.mysite.cloudfilestorage.security;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {
    public UserPrincipal getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return (UserPrincipal) securityContext.getAuthentication().getPrincipal();
    }
}
