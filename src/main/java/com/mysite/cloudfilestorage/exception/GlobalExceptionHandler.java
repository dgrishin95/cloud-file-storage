package com.mysite.cloudfilestorage.exception;

import com.mysite.cloudfilestorage.dto.ErrorMessageResponse;
import com.mysite.cloudfilestorage.exception.minio.DirectoryAlreadyExistsException;
import com.mysite.cloudfilestorage.exception.minio.InvalidOperationException;
import com.mysite.cloudfilestorage.exception.minio.InvalidPathException;
import com.mysite.cloudfilestorage.exception.minio.InvalidQueryException;
import com.mysite.cloudfilestorage.exception.minio.InvalidRequestBodyException;
import com.mysite.cloudfilestorage.exception.minio.ParentDirectoryIsNotFoundException;
import com.mysite.cloudfilestorage.exception.minio.ResourceAlreadyExistsException;
import com.mysite.cloudfilestorage.exception.minio.ResourceIsNotFoundException;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorMessageResponse handleInvalidArgument(MethodArgumentNotValidException ex) {
        return new ErrorMessageResponse(Objects.requireNonNull(ex.getFieldError()).getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({UserAlreadyExistsException.class, ResourceAlreadyExistsException.class, DirectoryAlreadyExistsException.class})
    public ErrorMessageResponse handleUserAlreadyExists(Exception ex) {
        return new ErrorMessageResponse(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UserIsNotExistsException.class)
    public ErrorMessageResponse handleUserIsNotExists(UserIsNotExistsException ex) {
        return new ErrorMessageResponse(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({InvalidPathException.class, InvalidOperationException.class,
            InvalidQueryException.class, InvalidRequestBodyException.class})
    public ErrorMessageResponse handleInvalidPath(Exception ex) {
        return new ErrorMessageResponse(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ResourceIsNotFoundException.class, ParentDirectoryIsNotFoundException.class})
    public ErrorMessageResponse handleResourceIsNotFound(ResourceIsNotFoundException ex) {
        return new ErrorMessageResponse(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorMessageResponse handleOtherExceptions(Exception ex) {
        if (ex instanceof AuthenticationException) {
            throw (AuthenticationException) ex;
        }

        return new ErrorMessageResponse("Unknown error");
    }
}
