package com.mysite.cloudfilestorage.service.minio;

import org.springframework.stereotype.Component;

@Component
public class MinioKeyBuilder {

    public String buildUserFileKey(Long userId, String path) {
        return "user-" + userId + "-files/" + path;
    }
}
