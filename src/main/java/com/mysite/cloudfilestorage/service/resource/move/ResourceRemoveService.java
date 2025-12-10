package com.mysite.cloudfilestorage.service.resource.move;

import com.mysite.cloudfilestorage.service.minio.MinioStorageService;
import com.mysite.cloudfilestorage.service.resource.common.ResourceKeyService;
import com.mysite.cloudfilestorage.service.resource.common.ResourceLookupService;
import com.mysite.cloudfilestorage.util.PathUtil;
import com.mysite.cloudfilestorage.validation.PathValidator;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceRemoveService {

    private final MinioStorageService minioStorageService;
    private final ResourceKeyService keyService;
    private final ResourceLookupService lookupService;
    private final PathValidator pathValidator;

    public void removeResource(String path) throws Exception {
        Long userId = keyService.getUserId();
        String key = keyService.getKey(userId, path);

        pathValidator.validatePath(path);

        if (PathUtil.isDirectory(path)) {
            removeDirectoryResource(key);
        } else {
            removeFileResource(key);
        }
    }

    private void removeFileResource(String key) throws Exception {
        StatObjectResponse fileStatResponseForRemove = lookupService.getFileStatResponse(key);
        minioStorageService.removeObject(fileStatResponseForRemove);
    }

    private void removeDirectoryResource(String key) throws Exception {
        List<Item> objects = minioStorageService.getListObjects(key, true);
        pathValidator.validateDirectoryIsEmpty(objects);

        minioStorageService.removeObjects(objects);
    }
}
