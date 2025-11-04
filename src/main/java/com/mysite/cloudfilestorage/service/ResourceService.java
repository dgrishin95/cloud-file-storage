package com.mysite.cloudfilestorage.service;

import com.mysite.cloudfilestorage.config.minio.MinioProperties;
import com.mysite.cloudfilestorage.dto.ResourceResponse;
import com.mysite.cloudfilestorage.dto.ResourceType;
import com.mysite.cloudfilestorage.exception.minio.InvalidPathException;
import com.mysite.cloudfilestorage.exception.minio.ResourceIsNotFoundException;
import com.mysite.cloudfilestorage.security.CurrentUserProvider;
import com.mysite.cloudfilestorage.validation.PathValidator;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final CurrentUserProvider currentUserProvider;
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final PathValidator pathValidator;

    public ResourceResponse getResource(String path) throws Exception {
        Long userId = currentUserProvider.getCurrentUser().getUser().getId();
        String key = "user-" + userId + "-files/" + path;

        if (pathValidator.isPathInvalid(path)) {
            throw new InvalidPathException("Invalid or missing path");
        }

        if (isPathDirectory(path)) {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .prefix(key)
                            .recursive(false)
                            .build());

            if (!results.iterator().hasNext()) {
                throw new ResourceIsNotFoundException("The resource was not found");
            }

            Item item = results.iterator().next().get();

            String objectName = item.objectName();

            int first = objectName.indexOf("/");
            int last = objectName.lastIndexOf("/");

            String folderPath = objectName.substring(first, last);
            last = folderPath.lastIndexOf("/");
            folderPath = folderPath.substring(1, last + 1);

            last = objectName.lastIndexOf("/");
            String name = objectName.substring(first, last);
            name = name.substring(name.lastIndexOf("/") + 1);
            return ResourceResponse.builder()
                    .path(folderPath)
                    .name(name)
                    .type(ResourceType.DIRECTORY)
                    .build();

        } else {
            try {
                StatObjectResponse statObjectResponse = minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(minioProperties.getBucket())
                                .object(key)
                                .build()
                );

                String objectName = statObjectResponse.object();

                String fileName = Paths.get(objectName).getFileName().toString();

                String folderPath = "";
                int firstSlashIndex = objectName.indexOf('/');
                int lastSlashIndex = objectName.lastIndexOf('/');
                if (lastSlashIndex != -1) {
                    folderPath = objectName.substring(firstSlashIndex + 1, lastSlashIndex + 1);
                }

                return ResourceResponse.builder()
                        .path(folderPath)
                        .name(fileName)
                        .size(statObjectResponse.size())
                        .type(ResourceType.FILE)
                        .build();
            } catch (ErrorResponseException exception) {
                throw new ResourceIsNotFoundException("The resource was not found");
            }
        }
    }

    private boolean isPathDirectory(String path) {
        return path.endsWith("/");
    }
}
