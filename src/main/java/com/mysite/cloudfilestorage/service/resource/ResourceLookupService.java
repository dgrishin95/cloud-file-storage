package com.mysite.cloudfilestorage.service.resource;

import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.service.minio.MinioStorageService;
import com.mysite.cloudfilestorage.validation.PathValidator;
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
}
