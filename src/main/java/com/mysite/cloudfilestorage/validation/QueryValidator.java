package com.mysite.cloudfilestorage.validation;

import com.mysite.cloudfilestorage.exception.minio.InvalidQueryException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class QueryValidator {

    public void validateQuery(String query) {
        if (StringUtils.isBlank(query)) {
            throw new InvalidQueryException("Invalid or missing query");
        }
    }
}
