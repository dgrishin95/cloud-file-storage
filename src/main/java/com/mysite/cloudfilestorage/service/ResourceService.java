package com.mysite.cloudfilestorage.service;

import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.dto.ResourceType;
import com.mysite.cloudfilestorage.security.CurrentUserProvider;
import com.mysite.cloudfilestorage.service.minio.MinioKeyBuilder;
import com.mysite.cloudfilestorage.service.minio.MinioStorageService;
import com.mysite.cloudfilestorage.util.PathUtil;
import com.mysite.cloudfilestorage.validation.PathValidator;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final CurrentUserProvider currentUserProvider;
    private final MinioKeyBuilder minioKeyBuilder;
    private final MinioStorageService minioStorageService;
    private final PathValidator pathValidator;

    public ResourceResponse getResource(String path) throws Exception {
        pathValidator.validatePath(path);

        Long userId = currentUserProvider.getCurrentUser().getUser().getId();
        String key = minioKeyBuilder.buildUserFileKey(userId, path);

        if (PathUtil.isDirectory(path)) {
            return getDirectoryResource(key);
        } else {
            return getFileResource(key);
        }
    }

    private ResourceResponse getDirectoryResource(String key) throws Exception {
        Item item = minioStorageService.getFirstOrThrow(key);

        String objectName = item.objectName();

        String folderPath = PathUtil.getPathForDirectory(objectName);
        String name = PathUtil.getNameForDirectory(objectName);

        return ResourceResponse.builder()
                .path(folderPath)
                .name(name)
                .type(ResourceType.DIRECTORY)
                .build();
    }

    private ResourceResponse getFileResource(String key) throws Exception {
        StatObjectResponse statObjectResponse = minioStorageService.getStatObjectResponse(key);

        String objectName = statObjectResponse.object();

        String folderPath = PathUtil.getPathForFile(objectName);
        String name = PathUtil.getNameForFile(objectName);

        return ResourceResponse.builder()
                .path(folderPath)
                .name(name)
                .size(statObjectResponse.size())
                .type(ResourceType.FILE)
                .build();
    }

    public void removeResource(String path) throws Exception {
        pathValidator.validatePath(path);

        Long userId = currentUserProvider.getCurrentUser().getUser().getId();
        String key = minioKeyBuilder.buildUserFileKey(userId, path);

        if (PathUtil.isDirectory(path)) {
            removeDirectoryResource(key);
        } else {
            removeFileResource(key);
        }
    }

    private void removeFileResource(String key) throws Exception {
        minioStorageService.removeObject(key);
    }

    private void removeDirectoryResource(String key) throws Exception {
        minioStorageService.removeObjects(key);
    }
}
