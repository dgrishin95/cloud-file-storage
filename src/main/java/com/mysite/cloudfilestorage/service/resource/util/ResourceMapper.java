package com.mysite.cloudfilestorage.service.resource.util;

import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.dto.ResourceType;
import com.mysite.cloudfilestorage.util.PathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceMapper {

    public ResourceResponse getDirectoryResourceResponse(String objectName) {
        String folderPath = PathUtil.getPathForDirectory(objectName);
        String name = PathUtil.getNameForDirectory(objectName);

        return ResourceResponse.builder()
                .path(folderPath)
                .name(name)
                .type(ResourceType.DIRECTORY)
                .build();
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
