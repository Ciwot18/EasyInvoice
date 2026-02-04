package com.kernith.easyinvoice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.config.WebConfig;
import com.kernith.easyinvoice.data.dto.invoiceitem.CreateInvoiceItemRequest;
import com.kernith.easyinvoice.data.dto.invoiceitem.UpdateInvoiceItemRequest;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.DiscountType;
import com.kernith.easyinvoice.data.model.Invoice;
import com.kernith.easyinvoice.data.model.InvoiceItem;
import com.kernith.easyinvoice.helper.CurrentUserArgumentResolver;
import com.kernith.easyinvoice.service.InvoiceItemService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvoiceItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        WebConfig.class,
        CurrentUserArgumentResolver.class,
        InvoiceItemControllerTests.TestExceptionHandler.class
})
@ActiveProfiles("test")
class InvoiceItemControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private InvoiceItemService invoiceItemService;

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
    class addInvoiceItemTests {
        @Test
        void addInvoiceItemReturnsOkWhenValid() throws Exception {
            setPrincipal();
            CreateInvoiceItemRequest req = new CreateInvoiceItemRequest(
                    1,
                    "Item",
                    null,
                    BigDecimal.ONE,
                    null,
                    BigDecimal.TEN,
                    BigDecimal.ZERO,
                    DiscountType.NONE,
                    BigDecimal.ZERO
            );
            when(invoiceItemService.addInvoiceItem(eq(77L), any(CreateInvoiceItemRequest.class), any(AuthPrincipal.class)))
                    .thenReturn(buildInvoiceItem());

            mockMvc.perform(post("/invoices/77/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(55L))
                    .andExpect(jsonPath("$.position").value(1))
                    .andExpect(jsonPath("$.description").value("Item"));
        }

        @Test
        void addInvoiceItemReturnsBadRequestWhenInvalid() throws Exception {
            setPrincipal();
            CreateInvoiceItemRequest req = new CreateInvoiceItemRequest(
                    1,
                    "",
                    null,
                    BigDecimal.ONE,
                    null,
                    BigDecimal.TEN,
                    BigDecimal.ZERO,
                    DiscountType.NONE,
                    BigDecimal.ZERO
            );

            mockMvc.perform(post("/invoices/77/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void addInvoiceItemReturnsServerErrorWhenServiceFails() throws Exception {
            setPrincipal();
            CreateInvoiceItemRequest req = new CreateInvoiceItemRequest(
                    1,
                    "Item",
                    null,
                    BigDecimal.ONE,
                    null,
                    BigDecimal.TEN,
                    BigDecimal.ZERO,
                    DiscountType.NONE,
                    BigDecimal.ZERO
            );
            when(invoiceItemService.addInvoiceItem(eq(77L), any(CreateInvoiceItemRequest.class), any(AuthPrincipal.class)))
                    .thenThrow(new RuntimeException("boom"));

            mockMvc.perform(post("/invoices/77/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class listInvoiceItemsTests {
        @Test
        void listInvoiceItemsReturnsNoContentWhenEmpty() throws Exception {
            setPrincipal();
            when(invoiceItemService.listInvoiceItems(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/invoices/77/items"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void listInvoiceItemsReturnsItemsWhenPresent() throws Exception {
            setPrincipal();
            when(invoiceItemService.listInvoiceItems(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(List.of(buildInvoiceItem()));

            mockMvc.perform(get("/invoices/77/items"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(55L))
                    .andExpect(jsonPath("$[0].description").value("Item"));
        }

        @Test
        void listInvoiceItemsReturnsServerErrorOnFailure() throws Exception {
            setPrincipal();
            when(invoiceItemService.listInvoiceItems(eq(77L), any(AuthPrincipal.class)))
                    .thenThrow(new RuntimeException("boom"));

            mockMvc.perform(get("/invoices/77/items"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class updateInvoiceItemTests {
        @Test
        void updateInvoiceItemReturnsOkWhenValid() throws Exception {
            setPrincipal();
            UpdateInvoiceItemRequest req = new UpdateInvoiceItemRequest(
                    2,
                    "Updated",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            when(invoiceItemService.updateInvoiceItem(eq(77L), eq(55L), any(UpdateInvoiceItemRequest.class), any(AuthPrincipal.class)))
                    .thenReturn(buildInvoiceItem());

            mockMvc.perform(patch("/invoices/77/items/55")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(55L));
        }

        @Test
        void updateInvoiceItemReturnsBadRequestWhenInvalid() throws Exception {
            setPrincipal();
            String longUnit = "x".repeat(21);
            UpdateInvoiceItemRequest req = new UpdateInvoiceItemRequest(
                    2,
                    "Updated",
                    null,
                    null,
                    longUnit,
                    null,
                    null,
                    null,
                    null
            );

            mockMvc.perform(patch("/invoices/77/items/55")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void updateInvoiceItemReturnsServerErrorWhenServiceFails() throws Exception {
            setPrincipal();
            UpdateInvoiceItemRequest req = new UpdateInvoiceItemRequest(
                    2,
                    "Updated",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            when(invoiceItemService.updateInvoiceItem(eq(77L), eq(55L), any(UpdateInvoiceItemRequest.class), any(AuthPrincipal.class)))
                    .thenThrow(new RuntimeException("boom"));

            mockMvc.perform(patch("/invoices/77/items/55")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class deleteInvoiceItemTests {
        @Test
        void deleteInvoiceItemReturnsNoContentWhenDeleted() throws Exception {
            setPrincipal();
            when(invoiceItemService.deleteInvoiceItem(eq(77L), eq(55L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.of(Boolean.TRUE));

            mockMvc.perform(delete("/invoices/77/items/55"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void deleteInvoiceItemReturnsBadRequestWhenMissing() throws Exception {
            setPrincipal();
            when(invoiceItemService.deleteInvoiceItem(eq(77L), eq(55L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(delete("/invoices/77/items/55"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void deleteInvoiceItemReturnsServerErrorWhenServiceFails() throws Exception {
            setPrincipal();
            when(invoiceItemService.deleteInvoiceItem(eq(77L), eq(55L), any(AuthPrincipal.class)))
                    .thenThrow(new RuntimeException("boom"));

            mockMvc.perform(delete("/invoices/77/items/55"))
                    .andExpect(status().isInternalServerError());
        }
    }

    private InvoiceItem buildInvoiceItem() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Invoice invoice = new Invoice(company, customer);
        ReflectionTestUtils.setField(invoice, "id", 77L);
        InvoiceItem item = new InvoiceItem(
                invoice,
                1,
                "Item",
                BigDecimal.ONE,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                DiscountType.NONE,
                BigDecimal.ZERO
        );
        ReflectionTestUtils.setField(item, "id", 55L);
        return item;
    }
}
