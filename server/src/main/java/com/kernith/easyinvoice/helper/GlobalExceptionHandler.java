package com.kernith.easyinvoice.helper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps application exceptions to HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Converts invalid state transitions to a {@code 400 Bad Request} with a readable message.
     *
     * @param ex thrown exception
     * @return error response with message body
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = "Invalid state transition";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}
