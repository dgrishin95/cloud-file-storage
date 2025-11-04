package com.mysite.cloudfilestorage.exception.minio;

public class ResourceIsNotFoundException extends RuntimeException {
    public ResourceIsNotFoundException(String message) {
        super(message);
    }
}
