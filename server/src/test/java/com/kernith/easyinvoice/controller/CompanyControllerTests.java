package com.kernith.easyinvoice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.config.WebConfig;
import com.kernith.easyinvoice.data.dto.company.CompanyDetailResponse;
import com.kernith.easyinvoice.data.dto.company.CompanySummaryResponse;
import com.kernith.easyinvoice.data.dto.company.CreateCompanyManagerRequest;
import com.kernith.easyinvoice.data.dto.company.CreateCompanyRequest;
import com.kernith.easyinvoice.data.dto.user.UserSummary;
import com.kernith.easyinvoice.data.model.UserRole;
import com.kernith.easyinvoice.helper.CurrentUserArgumentResolver;
import com.kernith.easyinvoice.service.CompanyService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        WebConfig.class,
        CurrentUserArgumentResolver.class,
        CompanyControllerTests.TestExceptionHandler.class
})
@ActiveProfiles("test")
class CompanyControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CompanyService companyService;

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
        AuthPrincipal principal = new AuthPrincipal(1L, 1L, "PLATFORM_ADMIN", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
        );
    }

    @Nested
    class createCompanyTests {
        @Test
        void createCompanyReturnsCreatedWhenValid() throws Exception {
            setPrincipal();
            CreateCompanyRequest req = new CreateCompanyRequest("Acme SRL", "IT123");
            CompanySummaryResponse res = new CompanySummaryResponse(10L, "Acme SRL", "IT123", null);
            when(companyService.createCompany(any(CreateCompanyRequest.class), any(AuthPrincipal.class)))
                    .thenReturn(res);

            mockMvc.perform(post("/api/platform/companies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(10L))
                    .andExpect(jsonPath("$.name").value("Acme SRL"))
                    .andExpect(jsonPath("$.vatNumber").value("IT123"));
        }

        @Test
        void createCompanyReturnsServerErrorWhenServiceThrows() throws Exception {
            setPrincipal();
            CreateCompanyRequest req = new CreateCompanyRequest("Acme SRL", "IT123");
            when(companyService.createCompany(any(CreateCompanyRequest.class), any(AuthPrincipal.class)))
                    .thenThrow(new RuntimeException("boom"));

            mockMvc.perform(post("/api/platform/companies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Internal error"));
        }
    }

    @Nested
    class listCompaniesTests {
        @Test
        void listCompaniesReturnsCompaniesWhenNotEmpty() throws Exception {
            setPrincipal();
            CompanySummaryResponse res = new CompanySummaryResponse(10L, "Acme SRL", "IT123", null);
            when(companyService.listCompanies(any(AuthPrincipal.class)))
                    .thenReturn(List.of(res));

            mockMvc.perform(get("/api/platform/companies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(10L))
                    .andExpect(jsonPath("$[0].name").value("Acme SRL"))
                    .andExpect(jsonPath("$[0].vatNumber").value("IT123"));
        }

        @Test
        void listCompaniesReturnsNoContentWhenEmpty() throws Exception {
            setPrincipal();
            when(companyService.listCompanies(any(AuthPrincipal.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/platform/companies"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    class getCompanyTests {
        @Test
        void getCompanyReturnsCompanyWhenFound() throws Exception {
            setPrincipal();
            CompanyDetailResponse res = new CompanyDetailResponse(10L, "Acme SRL", "IT123", null);
            when(companyService.getCompany(eq(10L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.of(res));

            mockMvc.perform(get("/api/platform/companies/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10L))
                    .andExpect(jsonPath("$.name").value("Acme SRL"))
                    .andExpect(jsonPath("$.vatNumber").value("IT123"));
        }

        @Test
        void getCompanyReturnsNotFoundWhenMissing() throws Exception {
            setPrincipal();
            when(companyService.getCompany(eq(10L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/api/platform/companies/10"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class createCompanyManagerTests {
        @Test
        void createCompanyManagerReturnsCreatedWhenFound() throws Exception {
            setPrincipal();
            CreateCompanyManagerRequest req = new CreateCompanyManagerRequest("manager@acme.test", "password123");
            UserSummary res = new UserSummary(77L, "manager@acme.test", UserRole.COMPANY_MANAGER, true);
            when(companyService.createCompanyManager(eq(10L), any(CreateCompanyManagerRequest.class), any(AuthPrincipal.class)))
                    .thenReturn(Optional.of(res));

            mockMvc.perform(post("/api/platform/companies/10/managers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(77L))
                    .andExpect(jsonPath("$.email").value("manager@acme.test"))
                    .andExpect(jsonPath("$.role").value("COMPANY_MANAGER"))
                    .andExpect(jsonPath("$.enabled").value(true));
        }

        @Test
        void createCompanyManagerReturnsNotFoundWhenMissing() throws Exception {
            setPrincipal();
            CreateCompanyManagerRequest req = new CreateCompanyManagerRequest("manager@acme.test", "password123");
            when(companyService.createCompanyManager(eq(10L), any(CreateCompanyManagerRequest.class), any(AuthPrincipal.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(post("/api/platform/companies/10/managers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNotFound());
        }
    }
}
