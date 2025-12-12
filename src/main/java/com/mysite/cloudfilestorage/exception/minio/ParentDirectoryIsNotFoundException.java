package com.mysite.cloudfilestorage.exception.minio;

public class ParentDirectoryIsNotFoundException extends RuntimeException {
    public ParentDirectoryIsNotFoundException(String message) {
        super(message);
    }
}
