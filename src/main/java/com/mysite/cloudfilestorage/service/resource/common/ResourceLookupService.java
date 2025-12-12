package com.mysite.cloudfilestorage.service.resource.common;

import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.exception.minio.ResourceIsNotFoundException;
import com.mysite.cloudfilestorage.mapper.ResourceResponseMapper;
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
public class ResourceLookupService {

    private final MinioStorageService minioStorageService;
    private final PathValidator pathValidator;
    private final ResourceResponseMapper responseMapper;

    public ResourceResponse getDirectoryResource(String key) throws Exception {
        List<Item> objects = minioStorageService.getListObjects(key, false);
        pathValidator.validateDirectoryIsEmpty(objects);

        return responseMapper.toDirectoryResourceResponse(key);
    }

    public ResourceResponse getFileResource(String key) throws Exception {
        StatObjectResponse fileStatResponse = getFileStatResponse(key);

        String objectName = fileStatResponse.object();

        String folderPath = PathUtil.getParentPathOfFile(objectName);
        String name = PathUtil.getNameForFile(objectName);

        return responseMapper.toFileResourceResponse(folderPath, name, fileStatResponse.size());
    }

    public StatObjectResponse getFileStatResponse(String key) throws Exception {
        try {
            return minioStorageService.getStatObjectResponse(key);
        } catch (ErrorResponseException exception) {
            throw new ResourceIsNotFoundException("The resource was not found");
        }
    }
}
