package com.mysite.cloudfilestorage.service.minio;

import com.mysite.cloudfilestorage.config.minio.MinioProperties;
import com.mysite.cloudfilestorage.exception.minio.ResourceIsNotFoundException;
import com.mysite.cloudfilestorage.util.PathUtil;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public Item getFirstOrThrow(String key) throws Exception {
        return getListObjects(key, false).getFirst();
    }

    public List<Item> getListObjects(String key, boolean recursive) throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .prefix(key)
                        .recursive(recursive)
                        .build());

        List<Item> items = new ArrayList<>();
        for (Result<Item> result : results) {
            items.add(result.get());
        }

        if (items.isEmpty()) {
            throw new ResourceIsNotFoundException("The resource was not found");
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

    public void removeObject(String key) throws Exception {
        StatObjectResponse objectForRemove = getStatObjectResponse(key);

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(objectForRemove.object())
                        .build());
    }

    public void removeObjects(String key) throws Exception {
        List<DeleteObject> objectsForRemove = getListObjects(key, true)
                .stream()
                .map(item -> new DeleteObject(item.objectName()))
                .toList();

        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .objects(objectsForRemove)
                        .build());

        for (Result<DeleteError> result : results) {
            result.get();
        }
    }

    public InputStream downloadObject(String key) throws Exception {
        return getObject(key);
    }

    public ByteArrayInputStream downloadObjects(String path, String key) throws Exception {
        List<Item> objects = getListObjects(key, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        for (Item item : objects) {
            String fullNameOfDownloadedFile = item.objectName();
            String nameForDownloadedFile = PathUtil.getNameForDownloadedFile(path, fullNameOfDownloadedFile);

            InputStream downloadedFile = getObject(fullNameOfDownloadedFile);
            ZipEntry entry = new ZipEntry(nameForDownloadedFile);
            zipOutputStream.putNextEntry(entry);
            downloadedFile.transferTo(zipOutputStream);

            zipOutputStream.closeEntry();
        }

        zipOutputStream.close();
        byteArrayOutputStream.close();

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    public InputStream getObject(String key) throws Exception {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(key)
                            .build());
        } catch (ErrorResponseException exception) {
            throw new ResourceIsNotFoundException("The resource was not found");
        }
    }
}
