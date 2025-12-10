package com.mysite.cloudfilestorage.service.resource;

import com.mysite.cloudfilestorage.dto.DownloadResult;
import com.mysite.cloudfilestorage.exception.minio.ResourceIsNotFoundException;
import com.mysite.cloudfilestorage.security.CurrentUserProvider;
import com.mysite.cloudfilestorage.service.minio.MinioKeyBuilder;
import com.mysite.cloudfilestorage.service.minio.MinioStorageService;
import com.mysite.cloudfilestorage.util.PathUtil;
import com.mysite.cloudfilestorage.validation.PathValidator;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceDownloadService {

    private final CurrentUserProvider currentUserProvider;
    private final MinioKeyBuilder minioKeyBuilder;
    private final MinioStorageService minioStorageService;
    private final PathValidator pathValidator;

    public DownloadResult downloadResource(String path) throws Exception {
        Long userId = currentUserProvider.getCurrentUser().getUser().getId();
        String key = minioKeyBuilder.buildUserFileKey(userId, path);
        String userDirectoryName = minioKeyBuilder.buildUserDirectoryName(userId);

        pathValidator.validatePath(path);

        if (PathUtil.isDirectory(path)) {
            String zipName;
            if (key.equals(userDirectoryName)) {
                zipName = PathUtil.DEFAULT_USER_DIRECTORY_NAME + ".zip";
                return new DownloadResult(zipName, downloadDirectoryResource(key, key));
            } else {
                zipName = PathUtil.getNameForDownloadedDirectory(path) + ".zip";
                return new DownloadResult(zipName, downloadDirectoryResource(path, key));
            }
        } else {
            String fileName = PathUtil.getNameForFile(path);
            return new DownloadResult(fileName, downloadFileReResource(key));
        }
    }

    private InputStream downloadFileReResource(String key) throws Exception {
        try {
            return minioStorageService.downloadObject(key);
        } catch (ErrorResponseException exception) {
            throw new ResourceIsNotFoundException("The resource was not found");
        }
    }

    private ByteArrayInputStream downloadDirectoryResource(String path, String key) throws Exception {
        List<Item> objects = minioStorageService.getListObjects(key, true);
        pathValidator.validateDirectoryIsEmpty(objects);

        try {
            return minioStorageService.downloadObjects(path, objects);
        } catch (ErrorResponseException exception) {
            throw new ResourceIsNotFoundException("The resource was not found");
        }
    }
}
