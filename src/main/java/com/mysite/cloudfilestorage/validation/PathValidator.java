package com.mysite.cloudfilestorage.validation;

import com.mysite.cloudfilestorage.exception.minio.InvalidPathException;
import com.mysite.cloudfilestorage.exception.minio.InvalidQueryException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PathValidator {

    public void validatePath(String path) {
        if (StringUtils.isBlank(path)
                || path.contains("..")
                || path.contains("//")
                || path.startsWith("/")) {
            throw new InvalidPathException("Invalid or missing path");
        }
    }

    public void validateQuery(String query) {
        if (StringUtils.isBlank(query)) {
            throw new InvalidQueryException("Invalid or missing query");
        }
    }
}
