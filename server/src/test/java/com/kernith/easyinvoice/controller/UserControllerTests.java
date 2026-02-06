package com.kernith.easyinvoice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.config.WebConfig;
import com.kernith.easyinvoice.data.dto.user.CreateBackofficeUserRequest;
import com.kernith.easyinvoice.data.model.User;
import com.kernith.easyinvoice.data.model.UserRole;
import com.kernith.easyinvoice.helper.CurrentUserArgumentResolver;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        WebConfig.class,
        CurrentUserArgumentResolver.class,
        UserControllerTests.TestExceptionHandler.class
})
@ActiveProfiles("test")
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    private void setPrincipal() {
        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
        );
    }

    @Nested
    class createBackofficeUserTests {
        @Test
        void createBackofficeUserReturnsCreatedWhenValid() throws Exception {
            setPrincipal();
            CreateBackofficeUserRequest req = new CreateBackofficeUserRequest("new@acme.test", "Filippo Conte", "password123");
            User user = mock(User.class);
            when(user.getId()).thenReturn(55L);
            when(user.getEmail()).thenReturn("new@acme.test");
            when(user.getRole()).thenReturn(UserRole.BACK_OFFICE);
            when(user.isEnabled()).thenReturn(true);
            when(userService.createBackofficeUser(any(CreateBackofficeUserRequest.class), any(AuthPrincipal.class)))
                    .thenReturn(user);

            mockMvc.perform(post("/manager/backoffice-users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(55L))
                    .andExpect(jsonPath("$.email").value("new@acme.test"))
                    .andExpect(jsonPath("$.role").value("BACK_OFFICE"))
                    .andExpect(jsonPath("$.enabled").value(true));
        }

        @Test
        void createBackofficeUserReturnsNotFoundWhenServiceReturnsNull() throws Exception {
            setPrincipal();
            CreateBackofficeUserRequest req = new CreateBackofficeUserRequest("new@acme.test", "Filippo Conte", "password123");
            when(userService.createBackofficeUser(any(CreateBackofficeUserRequest.class), any(AuthPrincipal.class)))
                    .thenReturn(null);

            mockMvc.perform(post("/manager/backoffice-users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class listCompanyUsersTests {
        @Test
        void listCompanyUsersReturnsUsersWhenNotEmpty() throws Exception {
            setPrincipal();
            User user = mock(User.class);
            when(user.getId()).thenReturn(1L);
            when(user.getEmail()).thenReturn("user@acme.test");
            when(user.getRole()).thenReturn(UserRole.BACK_OFFICE);
            when(user.isEnabled()).thenReturn(true);
            when(userService.listCompanyUsers(any(AuthPrincipal.class)))
                    .thenReturn(List.of(user));

            mockMvc.perform(get("/manager/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].email").value("user@acme.test"))
                    .andExpect(jsonPath("$[0].role").value("BACK_OFFICE"))
                    .andExpect(jsonPath("$[0].enabled").value(true));
        }

        @Test
        void listCompanyUsersReturnsNoContentWhenEmpty() throws Exception {
            setPrincipal();
            when(userService.listCompanyUsers(any(AuthPrincipal.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/manager/users"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    class listPlatformUsersTests {
        @Test
        void listPlatformUsersReturnsUsersWhenNotEmpty() throws Exception {
            setPrincipal();
            User user = mock(User.class);
            when(user.getId()).thenReturn(2L);
            when(user.getEmail()).thenReturn("adminlist@acme.test");
            when(user.getRole()).thenReturn(UserRole.COMPANY_MANAGER);
            when(user.isEnabled()).thenReturn(true);
            when(userService.listPlatformUsers(any(AuthPrincipal.class)))
                    .thenReturn(List.of(user));

            mockMvc.perform(get("/platform/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(2L))
                    .andExpect(jsonPath("$[0].email").value("adminlist@acme.test"))
                    .andExpect(jsonPath("$[0].role").value("COMPANY_MANAGER"))
                    .andExpect(jsonPath("$[0].enabled").value(true));
        }

        @Test
        void listPlatformUsersReturnsNoContentWhenEmpty() throws Exception {
            setPrincipal();
            when(userService.listPlatformUsers(any(AuthPrincipal.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/platform/users"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    class disableUserTests {
        @Test
        void disableUserReturnsNoContentWhenDisabled() throws Exception {
            setPrincipal();
            when(userService.disableUser(eq(9L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.of(Boolean.TRUE));

            mockMvc.perform(patch("/manager/users/9/disable"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void disableUserReturnsNotFoundWhenMissing() throws Exception {
            setPrincipal();
            when(userService.disableUser(eq(9L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(patch("/manager/users/9/disable"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class backofficeProfileTests {
        @Test
        void getBackofficeProfileReturnsProfileWhenPresent() throws Exception {
            setPrincipal();
            User user = mock(User.class);
            when(user.getId()).thenReturn(7L);
            when(user.getEmail()).thenReturn("user@acme.test");
            when(user.getRole()).thenReturn(UserRole.BACK_OFFICE);
            when(user.isEnabled()).thenReturn(true);
            when(userService.getBackofficeProfile(any(AuthPrincipal.class)))
                    .thenReturn(Optional.of(user));

            mockMvc.perform(get("/backoffice/profile"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(7L))
                    .andExpect(jsonPath("$.email").value("user@acme.test"))
                    .andExpect(jsonPath("$.role").value("BACK_OFFICE"))
                    .andExpect(jsonPath("$.enabled").value(true));
        }

        @Test
        void getBackofficeProfileReturnsUnauthorizedWhenMissing() throws Exception {
            setPrincipal();
            when(userService.getBackofficeProfile(any(AuthPrincipal.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/backoffice/profile"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
