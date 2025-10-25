package com.mysite.cloudfilestorage.exception;

import com.mysite.cloudfilestorage.dto.ErrorMessageResponse;
import java.util.Objects;
import org.springframework.http.HttpStatus;
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
}
