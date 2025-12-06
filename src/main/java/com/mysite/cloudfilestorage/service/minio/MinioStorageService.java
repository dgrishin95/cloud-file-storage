package com.mysite.cloudfilestorage.service.minio;

import com.mysite.cloudfilestorage.config.minio.MinioProperties;
import com.mysite.cloudfilestorage.util.PathUtil;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
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

        return items;
    }

    public StatObjectResponse getStatObjectResponse(String key) throws Exception {
        return minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(key)
                        .build()
        );
    }

    public void removeObject(StatObjectResponse statObjectResponseForRemove) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(statObjectResponseForRemove.object())
                        .build());
    }

    public void removeObjects(List<Item> objects) throws Exception {
        List<DeleteObject> objectsForRemove = objects
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

    public ByteArrayInputStream downloadObjects(String path, List<Item> objects) throws Exception {
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

    private InputStream getObject(String key) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(key)
                        .build());
    }

    public void copyObject(String oldKey, String newKey) throws Exception {
        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(newKey)
                        .source(
                                CopySource.builder()
                                        .bucket(minioProperties.getBucket())
                                        .object(oldKey)
                                        .build())
                        .build());
    }

    public void uploadObject(String key, InputStream inputStream, Long size) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .stream(inputStream, size, -1)
                        .object(key)
                        .build());
    }
}
