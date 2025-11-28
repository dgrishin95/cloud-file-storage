package com.mysite.cloudfilestorage.service;

import com.mysite.cloudfilestorage.dto.DownloadResult;
import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.dto.ResourceType;
import com.mysite.cloudfilestorage.exception.minio.InvalidOperationException;
import com.mysite.cloudfilestorage.exception.minio.ResourceAlreadyExistsException;
import com.mysite.cloudfilestorage.exception.minio.ResourceIsNotFoundException;
import com.mysite.cloudfilestorage.security.CurrentUserProvider;
import com.mysite.cloudfilestorage.service.minio.MinioKeyBuilder;
import com.mysite.cloudfilestorage.service.minio.MinioStorageService;
import com.mysite.cloudfilestorage.util.PathUtil;
import com.mysite.cloudfilestorage.validation.PathValidator;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
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
        List<Item> objects = minioStorageService.getListObjects(key, true);
        checkDirectoryIsEmpty(objects);

        Item item = objects.getFirst();

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
        return minioStorageService.downloadObject(key);
    }

    private ByteArrayInputStream downloadDirectoryResource(String path, String key) throws Exception {
        List<Item> objects = minioStorageService.getListObjects(key, true);
        checkDirectoryIsEmpty(objects);

        return minioStorageService.downloadObjects(path, objects);
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

        if (objectsNames.stream().anyMatch(newObjectsNames::contains)) {
            throw new ResourceAlreadyExistsException("The resource on the way to already exists");
        }

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
        minioStorageService.removeObject(oldKey);

        return getFileResource(newKey);
    }

    private void checkDirectoryIsEmpty(List<Item> items) {
        if (items.isEmpty()) {
            throw new ResourceIsNotFoundException("The resource was not found");
        }
    }
}
