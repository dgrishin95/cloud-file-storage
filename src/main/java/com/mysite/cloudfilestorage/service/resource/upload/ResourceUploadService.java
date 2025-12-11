package com.mysite.cloudfilestorage.service.resource.upload;

import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.dto.UploadResourceData;
import com.mysite.cloudfilestorage.mapper.ResourceResponseMapper;
import com.mysite.cloudfilestorage.service.minio.MinioStorageService;
import com.mysite.cloudfilestorage.service.resource.common.ResourceKeyService;
import com.mysite.cloudfilestorage.util.PathUtil;
import com.mysite.cloudfilestorage.validation.MultipartValidator;
import com.mysite.cloudfilestorage.validation.PathValidator;
import io.minio.messages.Item;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ResourceUploadService {

    private final MinioStorageService minioStorageService;
    private final ResourceKeyService keyService;
    private final ResourceResponseMapper responseMapper;
    private final PathValidator pathValidator;
    private final MultipartValidator multipartValidator;

    public List<ResourceResponse> uploadResource(String path, List<MultipartFile> resource) throws Exception {
        pathValidator.validatePath(path);
        pathValidator.validateIsDirectory(path);
        multipartValidator.validateUploadedResource(resource);

        Long userId = keyService.getUserId();

        List<UploadResourceData> uploadResourceData = resource.stream()
                .map(itemResource -> {
                    String originalFilename = itemResource.getOriginalFilename();
                    InputStream inputStream = multipartValidator.validateInputStream(itemResource);
                    String itemResourceObjectKey = keyService.getKey(userId, path + originalFilename);

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
                .map(resourceData -> responseMapper.toFileResourceResponse(
                        resourceData.folderPath(), resourceData.name(), resourceData.size()))
                .toList();
    }
}
