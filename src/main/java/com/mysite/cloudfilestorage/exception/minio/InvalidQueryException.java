package com.mysite.cloudfilestorage.exception.minio;

public class InvalidQueryException extends RuntimeException {
    public InvalidQueryException(String message) {
        super(message);
    }
}
