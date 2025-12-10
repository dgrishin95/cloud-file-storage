package com.mysite.cloudfilestorage.service.resource;

import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.exception.minio.InvalidOperationException;
import com.mysite.cloudfilestorage.exception.minio.ResourceAlreadyExistsException;
import com.mysite.cloudfilestorage.security.CurrentUserProvider;
import com.mysite.cloudfilestorage.service.minio.MinioKeyBuilder;
import com.mysite.cloudfilestorage.service.minio.MinioStorageService;
import com.mysite.cloudfilestorage.util.PathUtil;
import com.mysite.cloudfilestorage.validation.PathValidator;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceModificationService {

    private final CurrentUserProvider currentUserProvider;
    private final MinioKeyBuilder minioKeyBuilder;
    private final MinioStorageService minioStorageService;
    private final PathValidator pathValidator;
    private final ResourceMapper mapper;

    public void removeResource(String path) throws Exception {
        Long userId = currentUserProvider.getCurrentUser().getUser().getId();
        String key = minioKeyBuilder.buildUserFileKey(userId, path);

        pathValidator.validatePath(path);

        if (PathUtil.isDirectory(path)) {
            removeDirectoryResource(key);
        } else {
            removeFileResource(key);
        }
    }

    private void removeFileResource(String key) throws Exception {
        StatObjectResponse fileStatResponseForRemove = mapper.getFileStatResponse(key);
        minioStorageService.removeObject(fileStatResponseForRemove);
    }

    private void removeDirectoryResource(String key) throws Exception {
        List<Item> objects = minioStorageService.getListObjects(key, true);
        pathValidator.validateDirectoryIsEmpty(objects);

        minioStorageService.removeObjects(objects);
    }

    public ResourceResponse moveResource(String from, String to) throws Exception {
        pathValidator.validateFromPath(from);
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
        if (PathUtil.isPathEmpty(to)) {
            return mapper.getDirectoryResource(key);
        } else if (PathUtil.isMove(from, to) || PathUtil.isRename(from, to)) {
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

        return mapper.getDirectoryResource(newObjectsNames.getFirst());
    }

    private ResourceResponse moveFile(String oldKey, String newKey, String from, String to) throws Exception {
        if (PathUtil.isPathEmpty(to)) {
            return moveFileResourceToRootDirectory(oldKey, newKey);
        }
        if (PathUtil.isMove(from, to) || PathUtil.isRename(from, to)) {
            return moveFileResourceToDirectory(oldKey, newKey);
        } else {
            throw new InvalidOperationException("The paths differ");
        }
    }

    private ResourceResponse moveFileResourceToRootDirectory(String oldKey, String newKey) throws Exception {
        List<String> objectsNames = minioStorageService.getListObjects(newKey, false)
                .stream()
                .map(Item::objectName)
                .toList();

        String fileName = PathUtil.getNameForFile(oldKey);
        newKey = newKey + fileName;

        return moveFileResource(oldKey, newKey, objectsNames);
    }

    private ResourceResponse moveFileResourceToDirectory(String oldKey, String newKey) throws Exception {
        String folderPath = PathUtil.getNameDir(newKey);

        List<String> objectsNames = minioStorageService.getListObjects(folderPath, false)
                .stream()
                .map(Item::objectName)
                .toList();

        return moveFileResource(oldKey, newKey, objectsNames);
    }

    private ResourceResponse moveFileResource(String oldKey, String newKey, List<String> objectsNames) throws Exception {
        if (objectsNames.contains(newKey)) {
            throw new ResourceAlreadyExistsException("The resource on the way to already exists");
        }

        StatObjectResponse fileStatResponseForRemove = mapper.getFileStatResponse(oldKey);

        minioStorageService.copyObject(oldKey, newKey);
        minioStorageService.removeObject(fileStatResponseForRemove);

        return mapper.getFileResource(newKey);
    }
}
