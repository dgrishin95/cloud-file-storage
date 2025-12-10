package com.mysite.cloudfilestorage.service.resource.common;

import com.mysite.cloudfilestorage.security.CurrentUserProvider;
import com.mysite.cloudfilestorage.service.minio.MinioKeyBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceKeyService {

    private final CurrentUserProvider currentUserProvider;
    private final MinioKeyBuilder minioKeyBuilder;

    public Long getUserId() {
        return currentUserProvider.getCurrentUser().getUser().getId();
    }

    public String getKey(Long userId, String path) {
        return minioKeyBuilder.buildUserFileKey(userId, path);
    }
}
