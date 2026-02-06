package com.kernith.easyinvoice.controller;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.config.WebConfig;
import com.kernith.easyinvoice.data.dto.dashboard.AdminDashboardSummaryResponse;
import com.kernith.easyinvoice.data.dto.dashboard.CustomerInvoiceSummaryResponse;
import com.kernith.easyinvoice.data.dto.dashboard.InvoiceStatusAggregateResponse;
import com.kernith.easyinvoice.data.dto.dashboard.ManagerDashboardSummaryResponse;
import com.kernith.easyinvoice.data.dto.dashboard.QuoteStatusAggregateResponse;
import com.kernith.easyinvoice.data.model.InvoiceStatus;
import com.kernith.easyinvoice.data.model.QuoteStatus;
import com.kernith.easyinvoice.helper.CurrentUserArgumentResolver;
import com.kernith.easyinvoice.service.DashboardService;
import java.math.BigDecimal;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        WebConfig.class,
        CurrentUserArgumentResolver.class,
        DashboardControllerTests.TestExceptionHandler.class
})
@ActiveProfiles("test")
class DashboardControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

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

    private void setPrincipal(String role) {
        AuthPrincipal principal = new AuthPrincipal(1L, 2L, role, List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
        );
    }

    @Nested
    class managerSummaryTests {
        @Test
        void managerSummaryReturnsPayload() throws Exception {
            setPrincipal("COMPANY_MANAGER");
            ManagerDashboardSummaryResponse response = ManagerDashboardSummaryResponse.from(
                    List.of(QuoteStatusAggregateResponse.from(QuoteStatus.DRAFT, 1L, new BigDecimal("122.00"))),
                    List.of(InvoiceStatusAggregateResponse.from(InvoiceStatus.ISSUED, 1L, new BigDecimal("183.00"))),
                    new BigDecimal("244.00"),
                    new BigDecimal("183.00"),
                    new BigDecimal("183.00")
            );
            when(dashboardService.getManagerSummary(any(AuthPrincipal.class))).thenReturn(response);

            mockMvc.perform(get("/manager/dashboard/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quotes[0].status").value("DRAFT"))
                    .andExpect(jsonPath("$.invoices[0].status").value("ISSUED"))
                    .andExpect(jsonPath("$.paidTotal").value(244.00));
        }
    }

    @Nested
    class customerSummaryTests {
        @Test
        void customerInvoiceSummaryReturnsNotFoundWhenMissing() throws Exception {
            setPrincipal("COMPANY_MANAGER");
            when(dashboardService.getCustomerInvoiceSummary(eq(100L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/manager/customers/100/invoice-summary"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void customerInvoiceSummaryReturnsPayload() throws Exception {
            setPrincipal("COMPANY_MANAGER");
            CustomerInvoiceSummaryResponse response = CustomerInvoiceSummaryResponse.from(
                    100L,
                    List.of(InvoiceStatusAggregateResponse.from(InvoiceStatus.PAID, 1L, new BigDecimal("244.00"))),
                    new BigDecimal("244.00"),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
            when(dashboardService.getCustomerInvoiceSummary(eq(100L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.of(response));

            mockMvc.perform(get("/manager/customers/100/invoice-summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId").value(100L))
                    .andExpect(jsonPath("$.invoices[0].status").value("PAID"));
        }
    }

    @Nested
    class adminSummaryTests {
        @Test
        void adminSummaryReturnsPayload() throws Exception {
            setPrincipal("PLATFORM_ADMIN");
            AdminDashboardSummaryResponse response = AdminDashboardSummaryResponse.from(
                    2L,
                    5L,
                    4L,
                    1L,
                    1000L,
                    500L,
                    2048L,
                    1024L,
                    "/",
                    null,
                    "in-memory"
            );
            when(dashboardService.getAdminSummary(any(AuthPrincipal.class))).thenReturn(response);

            mockMvc.perform(get("/platform/dashboard/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.companies").value(2L))
                    .andExpect(jsonPath("$.dbPath").value("in-memory"));
        }
    }
}
