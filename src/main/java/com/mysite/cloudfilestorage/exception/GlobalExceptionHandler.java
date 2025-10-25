package com.mysite.cloudfilestorage.exception;

import com.mysite.cloudfilestorage.dto.ErrorMessageResponse;
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
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ErrorMessageResponse handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return new ErrorMessageResponse(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UserIsNotExistsException.class)
    public ErrorMessageResponse handleUserIsNotExists(UserIsNotExistsException ex) {
        return new ErrorMessageResponse(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorMessageResponse handleUserAlreadyExists(Exception ex) {
        if (ex instanceof AuthenticationException) {
            throw (AuthenticationException) ex;
        }

        return new ErrorMessageResponse("Unknown error");
    }
}
