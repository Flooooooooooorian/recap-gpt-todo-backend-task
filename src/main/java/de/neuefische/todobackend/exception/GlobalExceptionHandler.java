package de.neuefische.todobackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleNoSuchElementException(NoSuchElementException exception) {
        return new ErrorMessage(exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleUnExpectedExceptions(Exception exception) {
        return new ErrorMessage(exception.getMessage());
    }
}
