// src/main/java/com/example/authservice/web/GlobalExceptionHandler.java
package com.example.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String,Object> notFound(NoSuchElementException ex) {
        return Map.of("error", "not_found", "message", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // or BAD_REQUEST if you prefer 400
    public Map<String,Object> badCreds(IllegalArgumentException ex) {
        return Map.of("error", "invalid_credentials", "message", ex.getMessage());
    }
}
