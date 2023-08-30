package com.nus.tt02backend.exceptions;

import com.nus.tt02backend.models.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {BadRequestException.class})
    protected ErrorResponse badRequestExceptionHandler(BadRequestException ex, WebRequest webRequest) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(value = {NotFoundException.class})
    protected ErrorResponse notFoundExceptionHandler(NotFoundException ex, WebRequest webRequest) {
        return new ErrorResponse(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(),
                ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(value = {Exception.class})
    protected ErrorResponse generalExceptionHandler(Exception ex, WebRequest webRequest) {
        return new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getMessage(), LocalDateTime.now());
    }
}
