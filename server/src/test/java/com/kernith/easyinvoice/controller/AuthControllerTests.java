package com.kernith.easyinvoice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.config.WebConfig;
import com.kernith.easyinvoice.data.dto.auth.LoginRequest;
import com.kernith.easyinvoice.data.dto.auth.LoginResponse;
import com.kernith.easyinvoice.data.model.User;
import com.kernith.easyinvoice.data.model.UserRole;
import com.kernith.easyinvoice.helper.CurrentUserArgumentResolver;
import com.kernith.easyinvoice.service.AuthService;
import com.kernith.easyinvoice.service.UserService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        WebConfig.class,
        CurrentUserArgumentResolver.class,
        AuthControllerTests.TestExceptionHandler.class
})
@ActiveProfiles("test")
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(RuntimeException.class)
        ResponseEntity<String> handleRuntime(RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error");
        }
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class authLoginTests {
        @Test
        void loginReturnsTokenWhenCredentialsValid() throws Exception {
            LoginRequest req = new LoginRequest(10L, "user@acme.test", "pw");
            LoginResponse res = new LoginResponse("jwt", 7L, 10L, "COMPANY_MANAGER");
            when(authService.login(any(LoginRequest.class))).thenReturn(res);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt"))
                    .andExpect(jsonPath("$.userId").value(7L))
                    .andExpect(jsonPath("$.companyId").value(10L))
                    .andExpect(jsonPath("$.role").value("COMPANY_MANAGER"));
        }

        @Test
        void loginReturnsServerErrorWhenBodyIsInvalidJson() throws Exception {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid-json"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Internal error"));
        }

        @Test
        void loginThrowsWhenServiceThrows() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new RuntimeException("Invalid credentials"));

            LoginRequest req = new LoginRequest(10L, "user@acme.test", "bad");
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Internal error"));
        }

        @Test
        void loginReturnsServerErrorWhenServiceThrowsIllegalState() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new IllegalStateException("broken"));

            LoginRequest req = new LoginRequest(10L, "user@acme.test", "bad");
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Internal error"));
        }
    }

    @Nested
    class authMeTests {
        @Test
        void meReturnsProfileWhenUserExists() throws Exception {
            AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
            );

            User user = mock(User.class);
            when(user.getId()).thenReturn(7L);
            when(user.getEmail()).thenReturn("user@acme.test");
            when(user.getRole()).thenReturn(UserRole.BACK_OFFICE);
            when(user.isEnabled()).thenReturn(true);
            when(userService.getCurrentUser(any(AuthPrincipal.class))).thenReturn(Optional.of(user));

            mockMvc.perform(get("/auth/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(7L))
                    .andExpect(jsonPath("$.email").value("user@acme.test"))
                    .andExpect(jsonPath("$.role").value("BACK_OFFICE"))
                    .andExpect(jsonPath("$.enabled").value(true));
        }

        @Test
        void meReturnsUnauthorizedWhenUserNotFound() throws Exception {
            AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
            );
            when(userService.getCurrentUser(any(AuthPrincipal.class))).thenReturn(Optional.empty());

            mockMvc.perform(get("/auth/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void meThrowsWhenServiceThrows() throws Exception {
            AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
            );
            when(userService.getCurrentUser(any(AuthPrincipal.class)))
                    .thenThrow(new RuntimeException("boom"));

            mockMvc.perform(get("/auth/me"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Internal error"));
        }

        @Test
        void meReturnsServerErrorWhenServiceThrowsIllegalState() throws Exception {
            AuthPrincipal principal = new AuthPrincipal(7L, 10L, "BACK_OFFICE", List.of());
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
            );
            when(userService.getCurrentUser(any(AuthPrincipal.class)))
                    .thenThrow(new IllegalStateException("boom"));

            mockMvc.perform(get("/auth/me"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Internal error"));
        }
    }
}
