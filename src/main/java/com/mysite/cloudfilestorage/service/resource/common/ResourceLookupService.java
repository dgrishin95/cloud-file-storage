package com.mysite.cloudfilestorage.service.resource.common;

import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.exception.minio.ResourceIsNotFoundException;
import com.mysite.cloudfilestorage.service.minio.MinioStorageService;
import com.mysite.cloudfilestorage.service.resource.util.ResourceMapper;
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
public class ResourceLookupService {

    private final MinioStorageService minioStorageService;
    private final ResourceMapper mapper;
    private final PathValidator pathValidator;

    public ResourceResponse getDirectoryResource(String key) throws Exception {
        List<Item> objects = minioStorageService.getListObjects(key, false);
        pathValidator.validateDirectoryIsEmpty(objects);

        return mapper.getDirectoryResourceResponse(key);
    }

    public ResourceResponse getFileResource(String key) throws Exception {
        StatObjectResponse fileStatResponse = getFileStatResponse(key);

        String objectName = fileStatResponse.object();

        String folderPath = PathUtil.getPathForFile(objectName);
        String name = PathUtil.getNameForFile(objectName);

        return mapper.getFileResourceResponse(folderPath, name, fileStatResponse.size());
    }

    public StatObjectResponse getFileStatResponse(String key) throws Exception {
        try {
            return minioStorageService.getStatObjectResponse(key);
        } catch (ErrorResponseException exception) {
            throw new ResourceIsNotFoundException("The resource was not found");
        }
    }
}
