package com.kernith.easyinvoice.helper;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTests {

    @Test
    void handleIllegalStateUsesProvidedMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<String> response = handler.handleIllegalState(new IllegalStateException("bad"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("bad", response.getBody());
    }

    @Test
    void handleIllegalStateUsesDefaultMessageWhenBlank() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<String> response = handler.handleIllegalState(new IllegalStateException(" "));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid state transition", response.getBody());
    }
}
