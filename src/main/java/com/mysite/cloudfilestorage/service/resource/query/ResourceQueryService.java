package com.mysite.cloudfilestorage.service.resource.query;

import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.service.minio.MinioKeyBuilder;
import com.mysite.cloudfilestorage.service.minio.MinioStorageService;
import com.mysite.cloudfilestorage.service.resource.common.ResourceKeyService;
import com.mysite.cloudfilestorage.service.resource.common.ResourceLookupService;
import com.mysite.cloudfilestorage.service.resource.util.ResourceMapper;
import com.mysite.cloudfilestorage.util.PathUtil;
import com.mysite.cloudfilestorage.validation.PathValidator;
import com.mysite.cloudfilestorage.validation.QueryValidator;
import io.minio.messages.Item;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceQueryService {

    private final MinioKeyBuilder minioKeyBuilder;
    private final MinioStorageService minioStorageService;
    private final ResourceKeyService keyService;
    private final ResourceLookupService lookupService;
    private final ResourceMapper mapper;
    private final PathValidator pathValidator;
    private final QueryValidator queryValidator;

    public ResourceResponse getResource(String path) throws Exception {
        Long userId = keyService.getUserId();
        String key = keyService.getKey(userId, path);
        String userDirectoryName = minioKeyBuilder.buildUserDirectoryName(userId);

        pathValidator.validatePath(path);

        if (PathUtil.isDirectory(path)) {
            if (key.equals(userDirectoryName)) {
                return mapper.toDirectoryDefaultResourceResponse();
            }
            return lookupService.getDirectoryResource(key);
        } else {
            return lookupService.getFileResource(key);
        }
    }

    public List<ResourceResponse> getResourceForDirectory(String path) throws Exception {
        Long userId = keyService.getUserId();
        String key = keyService.getKey(userId, path);
        String userDirectoryName = minioKeyBuilder.buildUserDirectoryName(userId);

        pathValidator.validatePath(path);
        pathValidator.validateIsDirectory(path);

        List<Item> objects = minioStorageService.getListObjects(key, false);
        pathValidator.validateDirectoryIsEmpty(objects);

        List<String> objectsNames = new ArrayList<>(objects
                .stream()
                .map(Item::objectName)
                .toList());

        objectsNames.removeIf(objectsName -> objectsName.equals(userDirectoryName));

        return mapper.toDirectoryFilesResourceResponse(objectsNames, objectName -> true);
    }

    public List<ResourceResponse> searchResource(String query) throws Exception {
        queryValidator.validateQuery(query);

        Long userId = keyService.getUserId();
        String userDirectoryName = minioKeyBuilder.buildUserDirectoryName(userId);

        Set<String> subKeys = new HashSet<>();

        List<Item> listObjects = minioStorageService.getListObjects(userDirectoryName, true);

        for (Item item : listObjects) {
            List<String> foundSubKeys = PathUtil.getSubKeys(item.objectName(), query);
            subKeys.addAll(foundSubKeys);
        }

        return mapper.toDirectoryFilesResourceResponse(subKeys, subkey -> !subkey.equals(userDirectoryName));
    }
}
