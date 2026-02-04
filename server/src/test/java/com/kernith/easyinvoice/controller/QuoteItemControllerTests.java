package com.kernith.easyinvoice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.config.WebConfig;
import com.kernith.easyinvoice.data.dto.quoteitem.CreateQuoteItemRequest;
import com.kernith.easyinvoice.data.dto.quoteitem.UpdateQuoteItemRequest;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.Quote;
import com.kernith.easyinvoice.data.model.QuoteItem;
import com.kernith.easyinvoice.data.model.DiscountType;
import com.kernith.easyinvoice.helper.CurrentUserArgumentResolver;
import com.kernith.easyinvoice.service.QuoteItemService;
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

@WebMvcTest(QuoteItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        WebConfig.class,
        CurrentUserArgumentResolver.class,
        QuoteItemControllerTests.TestExceptionHandler.class
})
@ActiveProfiles("test")
class QuoteItemControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private QuoteItemService quoteItemService;

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
    class addQuoteItemTests {
        @Test
        void addQuoteItemReturnsOkWhenValid() throws Exception {
            setPrincipal();
            CreateQuoteItemRequest req = new CreateQuoteItemRequest(
                    1,
                    "Item",
                    null,
                    BigDecimal.ONE,
                    null,
                    BigDecimal.TEN,
                    BigDecimal.ZERO,
                    null,
                    null
            );
            when(quoteItemService.addQuoteItem(eq(77L), any(CreateQuoteItemRequest.class), any(AuthPrincipal.class)))
                    .thenReturn(buildQuoteItem());

            mockMvc.perform(post("/api/quotes/77/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(55L))
                    .andExpect(jsonPath("$.position").value(1))
                    .andExpect(jsonPath("$.description").value("Item"));
        }
    }

    @Nested
    class listQuoteItemsTests {
        @Test
        void listQuoteItemsReturnsNoContentWhenEmpty() throws Exception {
            setPrincipal();
            when(quoteItemService.listQuoteItems(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/quotes/77/items"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void listQuoteItemsReturnsItemsWhenPresent() throws Exception {
            setPrincipal();
            when(quoteItemService.listQuoteItems(eq(77L), any(AuthPrincipal.class)))
                    .thenReturn(List.of(buildQuoteItem()));

            mockMvc.perform(get("/api/quotes/77/items"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(55L))
                    .andExpect(jsonPath("$[0].description").value("Item"));
        }
    }

    @Nested
    class updateQuoteItemTests {
        @Test
        void updateQuoteItemReturnsOkWhenValid() throws Exception {
            setPrincipal();
            UpdateQuoteItemRequest req = new UpdateQuoteItemRequest(
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
            when(quoteItemService.updateQuoteItem(eq(77L), eq(55L), any(UpdateQuoteItemRequest.class), any(AuthPrincipal.class)))
                    .thenReturn(buildQuoteItem());

            mockMvc.perform(patch("/api/quotes/77/items/55")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(55L));
        }
    }

    @Nested
    class deleteQuoteItemTests {
        @Test
        void deleteQuoteItemReturnsNoContentWhenDeleted() throws Exception {
            setPrincipal();
            when(quoteItemService.deleteQuoteItem(eq(77L), eq(55L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.of(Boolean.TRUE));

            mockMvc.perform(delete("/api/quotes/77/items/55"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void deleteQuoteItemReturnsNotFoundWhenMissing() throws Exception {
            setPrincipal();
            when(quoteItemService.deleteQuoteItem(eq(77L), eq(55L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(delete("/api/quotes/77/items/55"))
                    .andExpect(status().isNotFound());
        }
    }

    private QuoteItem buildQuoteItem() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Quote quote = new Quote(company, customer);
        QuoteItem item = new QuoteItem(
                quote,
                1,
                "Item",
                BigDecimal.ONE,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                DiscountType.NONE,
                BigDecimal.ZERO
        );
        org.springframework.test.util.ReflectionTestUtils.setField(item, "id", 55L);
        return item;
    }
}
