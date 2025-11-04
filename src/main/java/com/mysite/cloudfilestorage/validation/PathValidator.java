package com.mysite.cloudfilestorage.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PathValidator {

    public boolean isPathInvalid(String path) {
        return StringUtils.isBlank(path)
                || path.contains("..")
                || path.contains("//")
                || path.startsWith("/");
    }
}
