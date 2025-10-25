package com.mysite.cloudfilestorage.exception;

public class UserIsNotExistsException extends RuntimeException {
    public UserIsNotExistsException(String message) {
        super(message);
    }
}
