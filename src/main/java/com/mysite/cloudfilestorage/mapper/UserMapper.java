package com.mysite.cloudfilestorage.mapper;

import com.mysite.cloudfilestorage.dto.AuthRequest;
import com.mysite.cloudfilestorage.dto.AuthResponse;
import com.mysite.cloudfilestorage.mapper.config.DefaultMapperConfig;
import com.mysite.cloudfilestorage.model.User;
import org.mapstruct.Mapper;

@Mapper(config = DefaultMapperConfig.class)
public interface UserMapper {
    User toUser(AuthRequest authRequest);

    AuthResponse toAuthResponse(User user);
}
