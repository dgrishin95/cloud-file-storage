package com.mysite.cloudfilestorage.service.resource;

import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.dto.ResourceType;
import com.mysite.cloudfilestorage.exception.minio.ResourceIsNotFoundException;
import com.mysite.cloudfilestorage.service.minio.MinioStorageService;
import com.mysite.cloudfilestorage.util.PathUtil;
import com.mysite.cloudfilestorage.validation.PathValidator;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceMapper {

    private final MinioStorageService minioStorageService;
    private final PathValidator pathValidator;

    public ResourceResponse getDirectoryResource(String key) throws Exception {
        List<Item> objects = minioStorageService.getListObjects(key, false);
        pathValidator.validateDirectoryIsEmpty(objects);

        return getDirectoryResourceResponse(key);
    }

    public ResourceResponse getDirectoryResourceResponse(String objectName) {
        String folderPath = PathUtil.getPathForDirectory(objectName);
        String name = PathUtil.getNameForDirectory(objectName);

        return ResourceResponse.builder()
                .path(folderPath)
                .name(name)
                .type(ResourceType.DIRECTORY)
                .build();
    }

    public ResourceResponse getFileResource(String key) throws Exception {
        StatObjectResponse fileStatResponse = getFileStatResponse(key);

        String objectName = fileStatResponse.object();

        String folderPath = PathUtil.getPathForFile(objectName);
        String name = PathUtil.getNameForFile(objectName);

        return getFileResourceResponse(folderPath, name, fileStatResponse.size());
    }

    public StatObjectResponse getFileStatResponse(String key) throws Exception {
        try {
            return minioStorageService.getStatObjectResponse(key);
        } catch (ErrorResponseException exception) {
            throw new ResourceIsNotFoundException("The resource was not found");
        }
    }

    public ResourceResponse getFileResourceResponse(String folderPath, String name, Long size) {
        return ResourceResponse.builder()
                .path(folderPath)
                .name(name)
                .size(size)
                .type(ResourceType.FILE)
                .build();
    }

    public ResourceResponse getDirectoryDefaultResourceResponse() {
        return ResourceResponse.builder()
                .path("")
                .name("")
                .type(ResourceType.DIRECTORY)
                .build();
    }
}
