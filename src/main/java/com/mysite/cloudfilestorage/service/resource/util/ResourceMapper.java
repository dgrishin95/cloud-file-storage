package com.mysite.cloudfilestorage.service.resource.util;

import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.dto.ResourceType;
import com.mysite.cloudfilestorage.exception.minio.ResourceIsNotFoundException;
import com.mysite.cloudfilestorage.mapper.ResourceResponseMapper;
import com.mysite.cloudfilestorage.service.resource.common.ResourceLookupService;
import com.mysite.cloudfilestorage.util.PathUtil;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceMapper {

    private final ResourceLookupService lookupService;
    private final ResourceResponseMapper responseMapper;

    public ResourceResponse toDirectoryDefaultResourceResponse() {
        return ResourceResponse.builder()
                .path("")
                .name("")
                .type(ResourceType.DIRECTORY)
                .build();
    }

    public List<ResourceResponse> toDirectoryFilesResourceResponse(Collection<String> keys, Predicate<String> filter) {
        return keys
                .stream()
                .filter(filter)
                .map(subKey -> {
                    if (PathUtil.isDirectory(subKey)) {
                        return responseMapper.toDirectoryResourceResponse(subKey);
                    }
                    try {
                        return lookupService.getFileResource(subKey);
                    } catch (Exception exception) {
                        throw new ResourceIsNotFoundException("The resource was not found");
                    }
                })
                .sorted(Comparator.comparing(ResourceResponse::getType)
                        .thenComparing(ResourceResponse::getPath)
                )
                .toList();
    }
}
