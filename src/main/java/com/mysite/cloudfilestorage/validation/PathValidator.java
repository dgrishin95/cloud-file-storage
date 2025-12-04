package com.mysite.cloudfilestorage.validation;

import com.mysite.cloudfilestorage.exception.minio.InvalidPathException;
import com.mysite.cloudfilestorage.exception.minio.ResourceAlreadyExistsException;
import com.mysite.cloudfilestorage.util.PathUtil;
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
}