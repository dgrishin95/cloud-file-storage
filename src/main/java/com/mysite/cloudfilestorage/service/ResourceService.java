package com.mysite.cloudfilestorage.service;

import com.mysite.cloudfilestorage.dto.DownloadResult;
import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.dto.ResourceType;
import com.mysite.cloudfilestorage.dto.UploadResourceData;
import com.mysite.cloudfilestorage.exception.minio.InvalidOperationException;
import com.mysite.cloudfilestorage.exception.minio.ResourceAlreadyExistsException;
import com.mysite.cloudfilestorage.exception.minio.ResourceIsNotFoundException;
import com.mysite.cloudfilestorage.security.CurrentUserProvider;
import com.mysite.cloudfilestorage.service.minio.MinioKeyBuilder;
import com.mysite.cloudfilestorage.service.minio.MinioStorageService;
import com.mysite.cloudfilestorage.util.PathUtil;
import com.mysite.cloudfilestorage.validation.MultipartValidator;
import com.mysite.cloudfilestorage.validation.PathValidator;
import com.mysite.cloudfilestorage.validation.QueryValidator;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final CurrentUserProvider currentUserProvider;
    private final MinioKeyBuilder minioKeyBuilder;
    private final MinioStorageService minioStorageService;
    private final PathValidator pathValidator;
    private final QueryValidator queryValidator;
    private final MultipartValidator multipartValidator;

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
        List<Item> objects = minioStorageService.getListObjects(key, true);
        checkDirectoryIsEmpty(objects);

        Item item = objects.getFirst();

        return getDirectoryResourceResponse(item.objectName());
    }

    private ResourceResponse getDirectoryResourceResponse(String objectName) {
        String folderPath = PathUtil.getPathForDirectory(objectName);
        String name = PathUtil.getNameForDirectory(objectName);

        return ResourceResponse.builder()
                .path(folderPath)
                .name(name)
                .type(ResourceType.DIRECTORY)
                .build();
    }

    private ResourceResponse getFileResource(String key) throws Exception {
        StatObjectResponse fileStatResponse = getFileStatResponse(key);

        String objectName = fileStatResponse.object();

        String folderPath = PathUtil.getPathForFile(objectName);
        String name = PathUtil.getNameForFile(objectName);

        return getFileResourceResponse(folderPath, name, fileStatResponse.size());
    }

    private StatObjectResponse getFileStatResponse(String key) throws Exception {
        try {
            return minioStorageService.getStatObjectResponse(key);
        } catch (ErrorResponseException exception) {
            throw new ResourceIsNotFoundException("The resource was not found");
        }
    }

    private ResourceResponse getFileResourceResponse(String folderPath, String name, Long size) {
        return ResourceResponse.builder()
                .path(folderPath)
                .name(name)
                .size(size)
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
        StatObjectResponse fileStatResponseForRemove = getFileStatResponse(key);
        minioStorageService.removeObject(fileStatResponseForRemove);
    }

    private void removeDirectoryResource(String key) throws Exception {
        List<Item> objects = minioStorageService.getListObjects(key, true);
        checkDirectoryIsEmpty(objects);

        minioStorageService.removeObjects(objects);
    }

    public DownloadResult downloadResource(String path) throws Exception {
        pathValidator.validatePath(path);

        Long userId = currentUserProvider.getCurrentUser().getUser().getId();
        String key = minioKeyBuilder.buildUserFileKey(userId, path);

        if (PathUtil.isDirectory(path)) {
            String zipName = PathUtil.getNameForDownloadedDirectory(path) + ".zip";
            return new DownloadResult(zipName, downloadDirectoryResource(path, key));
        } else {
            String fileName = PathUtil.getNameForFile(path);
            return new DownloadResult(fileName, downloadFileReResource(key));
        }
    }

    private InputStream downloadFileReResource(String key) throws Exception {
        try {
            return minioStorageService.downloadObject(key);
        } catch (ErrorResponseException exception) {
            throw new ResourceIsNotFoundException("The resource was not found");
        }
    }

    private ByteArrayInputStream downloadDirectoryResource(String path, String key) throws Exception {
        List<Item> objects = minioStorageService.getListObjects(key, true);
        checkDirectoryIsEmpty(objects);

        try {
            return minioStorageService.downloadObjects(path, objects);
        } catch (ErrorResponseException exception) {
            throw new ResourceIsNotFoundException("The resource was not found");
        }
    }

    public ResourceResponse moveResource(String from, String to) throws Exception {
        pathValidator.validatePath(from);
        pathValidator.validatePath(to);

        Long userId = currentUserProvider.getCurrentUser().getUser().getId();
        String key = minioKeyBuilder.buildUserFileKey(userId, from);

        if (PathUtil.isDirectory(from)) {
            return moveDirectory(key, from, to);
        } else {
            String newKey = minioKeyBuilder.buildUserFileKey(userId, to);
            return moveFile(key, newKey, from, to);
        }
    }

    private ResourceResponse moveDirectory(String key, String from, String to) throws Exception {
        if (PathUtil.isMove(from, to) || PathUtil.isRename(from, to)) {
            return moveDirectoryResource(key, from, to);
        } else {
            throw new InvalidOperationException("The paths differ");
        }
    }

    private ResourceResponse moveDirectoryResource(String key, String from, String to) throws Exception {
        List<Item> objects = minioStorageService.getListObjects(key, true);

        List<String> objectsNames = objects
                .stream()
                .map(Item::objectName)
                .toList();

        List<String> newObjectsNames = objectsNames
                .stream()
                .map(objectName -> PathUtil.getNewKeyForMovingFile(objectName, from, to))
                .toList();

        pathValidator.validateNewObjectsNamesForCreating(objectsNames, newObjectsNames);

        for (int i = 0; i < objectsNames.size(); i++) {
            minioStorageService.copyObject(objectsNames.get(i), newObjectsNames.get(i));
        }

        minioStorageService.removeObjects(objects);

        return getDirectoryResource(newObjectsNames.getFirst());
    }

    private ResourceResponse moveFile(String oldKey, String newKey, String from, String to) throws Exception {
        if (PathUtil.isMove(from, to) || PathUtil.isRename(from, to)) {
            return moveFileResource(oldKey, newKey);
        } else {
            throw new InvalidOperationException("The paths differ");
        }
    }

    private ResourceResponse moveFileResource(String oldKey, String newKey) throws Exception {
        String folderPath = PathUtil.getNameDir(newKey);

        List<String> objectsNames = minioStorageService.getListObjects(folderPath, true)
                .stream()
                .map(Item::objectName)
                .toList();

        if (objectsNames.contains(newKey)) {
            throw new ResourceAlreadyExistsException("The resource on the way to already exists");
        }

        minioStorageService.copyObject(oldKey, newKey);

        StatObjectResponse fileStatResponseForRemove = getFileStatResponse(oldKey);
        minioStorageService.removeObject(fileStatResponseForRemove);

        return getFileResource(newKey);
    }

    private void checkDirectoryIsEmpty(List<Item> items) {
        if (items.isEmpty()) {
            throw new ResourceIsNotFoundException("The resource was not found");
        }
    }

    public List<ResourceResponse> searchResource(String query) throws Exception {
        queryValidator.validateQuery(query);

        Long userId = currentUserProvider.getCurrentUser().getUser().getId();
        String userDirectoryName = minioKeyBuilder.buildUserDirectoryName(userId);

        Set<String> subKeys = new HashSet<>();

        List<Item> listObjects = minioStorageService.getListObjects(userDirectoryName, true);

        for (Item item : listObjects) {
            List<String> foundSubKeys = PathUtil.getSubKeys(item.objectName(), query);
            subKeys.addAll(foundSubKeys);
        }

        return subKeys
                .stream()
                .filter(s -> !s.equals(userDirectoryName))
                .map(subKey -> {
                    if (PathUtil.isDirectory(subKey)) {
                        return getDirectoryResourceResponse(subKey);
                    }
                    try {
                        return getFileResource(subKey);
                    } catch (Exception exception) {
                        throw new ResourceIsNotFoundException("The resource was not found");
                    }
                })
                .sorted(Comparator.comparing(ResourceResponse::getType))
                .toList();
    }

    public List<ResourceResponse> uploadResource(String path, List<MultipartFile> resource) throws Exception {
        pathValidator.validatePath(path);
        pathValidator.validateIsDirectory(path);
        multipartValidator.validateUploadedResource(resource);

        Long userId = currentUserProvider.getCurrentUser().getUser().getId();

        List<UploadResourceData> uploadResourceData = resource.stream()
                .map(itemResource -> {
                    String originalFilename = itemResource.getOriginalFilename();
                    InputStream inputStream = multipartValidator.validateInputStream(itemResource);
                    String itemResourceObjectKey = minioKeyBuilder.buildUserFileKey(userId, path + originalFilename);

                    return new UploadResourceData(
                            itemResourceObjectKey,
                            inputStream,
                            PathUtil.getPathForFile(itemResourceObjectKey),
                            PathUtil.getNameForFile(itemResourceObjectKey),
                            itemResource.getSize()
                    );
                })
                .toList();

        Set<String> uniqueKeysDirectories = uploadResourceData.stream()
                .map(resourceData -> PathUtil.getNameDir(resourceData.key()))
                .collect(Collectors.toSet());
        List<Item> pathObjects = new ArrayList<>();
        for (String uniqueKeysDirectory : uniqueKeysDirectories) {
            pathObjects.addAll(minioStorageService.getListObjects(uniqueKeysDirectory, true));
        }

        List<String> pathObjectsNames = pathObjects.stream()
                .map(Item::objectName)
                .toList();

        List<String> uploadedObjectsNames = uploadResourceData.stream()
                .map(UploadResourceData::key)
                .toList();

        pathValidator.validateNewObjectsNamesForCreating(pathObjectsNames, uploadedObjectsNames);

        for (UploadResourceData resourceData : uploadResourceData) {
            minioStorageService.uploadObject(resourceData.key(), resourceData.inputStream(), resourceData.size());
        }

        return uploadResourceData.stream()
                .map(resourceData -> getFileResourceResponse(
                        resourceData.folderPath(), resourceData.name(), resourceData.size()))
                .toList();
    }
}
