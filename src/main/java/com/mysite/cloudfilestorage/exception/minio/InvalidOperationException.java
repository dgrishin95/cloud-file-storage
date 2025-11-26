package com.mysite.cloudfilestorage.exception.minio;

public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException(String message) {
        super(message);
    }
}
