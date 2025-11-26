package com.mysite.cloudfilestorage.service;

import com.mysite.cloudfilestorage.dto.DownloadResult;
import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.dto.ResourceType;
import com.mysite.cloudfilestorage.exception.minio.InvalidOperationException;
import com.mysite.cloudfilestorage.exception.minio.ResourceAlreadyExistsException;
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
        return minioStorageService.downloadObjects(path, key);
    }

    // Если последняя часть (имя) совпадает, но различается путь до неё → это перемещение.
    // Если совпадает всё до последнего /, но отличается часть после него → это переименование.
    public ResourceResponse moveResource(String from, String to) throws Exception {
        pathValidator.validatePath(from);
        pathValidator.validatePath(to);

        Long userId = currentUserProvider.getCurrentUser().getUser().getId();
        String key = minioKeyBuilder.buildUserFileKey(userId, from);

        if (PathUtil.isDirectory(from)) {
            return moveDirectory(key, from, to);
        }

        return null;
    }

    private ResourceResponse moveDirectory(String key, String from, String to) throws Exception {
        if (PathUtil.isMove(from, to) || PathUtil.isRename(from, to)) {
            return moveDirectoryResource(key, from, to);
        } else {
            throw new InvalidOperationException("The paths differ");
        }
    }

    private ResourceResponse moveDirectoryResource(String key, String from, String to) throws Exception {
        List<String> objectsNames = minioStorageService.getListObjects(key, true)
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

        minioStorageService.removeObjects(key);

        return getDirectoryResource(newObjectsNames.getFirst());
    }
}
