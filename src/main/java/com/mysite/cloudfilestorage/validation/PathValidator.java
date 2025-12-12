package com.mysite.cloudfilestorage.validation;

import com.mysite.cloudfilestorage.exception.minio.DirectoryAlreadyExistsException;
import com.mysite.cloudfilestorage.exception.minio.InvalidPathException;
import com.mysite.cloudfilestorage.exception.minio.ParentDirectoryIsNotFoundException;
import com.mysite.cloudfilestorage.exception.minio.ResourceAlreadyExistsException;
import com.mysite.cloudfilestorage.exception.minio.ResourceIsNotFoundException;
import com.mysite.cloudfilestorage.util.PathUtil;
import io.minio.messages.Item;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PathValidator {

    public void validatePath(String path) {
        if (isInvalidPathComponent(path)) {
            throw new InvalidPathException("Invalid or missing path");
        }
    }

    public boolean isInvalidPathComponent(String pathComponent) {
        return pathComponent.contains("..")
                || pathComponent.contains("//")
                || pathComponent.startsWith("/");
    }

    public void validateInitialPath(String path) {
        if (isInvalidPathComponent(path) || path.isEmpty()) {
            throw new InvalidPathException("Invalid or missing path");
        }
    }

    public boolean isInvalidFileName(String pathComponent) {
        return StringUtils.isBlank(pathComponent)
                || pathComponent.contains("..")
                || pathComponent.contains("//")
                || pathComponent.startsWith("/");
    }

    public void validateNewObjectsNamesForCreating(List<String> objectsNames, List<String> newObjectsNames) {
        if (objectsNames.stream().anyMatch(newObjectsNames::contains)) {
            throw new ResourceAlreadyExistsException("The resource on the way to already exists");
        }
    }

    public void validateIsDirectory(String path) {
        if (!PathUtil.isDirectory(path)) {
            throw new InvalidPathException("Invalid or missing path");
        }
    }

    public void validateDirectoryIsEmpty(List<Item> items) {
        if (items.isEmpty()) {
            throw new ResourceIsNotFoundException("The resource was not found");
        }
    }

    public void validateDirectoryIsNotEmpty(List<Item> items) {
        if (!items.isEmpty()) {
            throw new DirectoryAlreadyExistsException("The directory already exists");
        }
    }

    public void validateParentDirectoryIsEmpty(List<Item> items) {
        if (items.isEmpty()) {
            throw new ParentDirectoryIsNotFoundException("The parent directory does not exist");
        }
    }
}