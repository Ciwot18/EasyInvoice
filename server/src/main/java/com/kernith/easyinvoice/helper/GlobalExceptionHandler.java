package com.kernith.easyinvoice.helper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = "Invalid state transition";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}