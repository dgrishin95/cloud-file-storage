package com.mysite.cloudfilestorage.service.minio;

import com.mysite.cloudfilestorage.config.minio.MinioProperties;
import com.mysite.cloudfilestorage.exception.minio.ResourceIsNotFoundException;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public Item getFirstOrThrow(String key) throws Exception {
        return getListObjects(key).getFirst();
    }

    public List<Item> getListObjects(String key) throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .prefix(key)
                        .recursive(false)
                        .build());

        List<Item> items = new ArrayList<>();
        for (Result<Item> result : results) {
            items.add(result.get());
        }

        return items;
    }

    public StatObjectResponse getStatObjectResponse(String key) throws Exception {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(key)
                            .build()
            );
        } catch (ErrorResponseException exception) {
            throw new ResourceIsNotFoundException("The resource was not found");
        }
    }
}
