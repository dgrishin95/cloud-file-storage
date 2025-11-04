package com.mysite.cloudfilestorage.exception.minio;

public class InvalidPathException extends RuntimeException {
    public InvalidPathException(String message) {
        super(message);
    }
}
