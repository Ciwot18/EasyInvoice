package com.kernith.easyinvoice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.config.WebConfig;
import com.kernith.easyinvoice.data.dto.customer.CreateCustomerRequest;
import com.kernith.easyinvoice.data.dto.customer.UpdateCustomerRequest;
import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.CustomerStatus;
import com.kernith.easyinvoice.helper.CurrentUserArgumentResolver;
import com.kernith.easyinvoice.service.CustomerService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        WebConfig.class,
        CurrentUserArgumentResolver.class,
        CustomerControllerTests.TestExceptionHandler.class
})
@ActiveProfiles("test")
class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CustomerService customerService;

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
    class createCustomerTests {
        @Test
        void createCustomerReturnsOkWhenValid() throws Exception {
            setPrincipal();
            CreateCustomerRequest req = new CreateCustomerRequest(
                    "Acme Spa",
                    "Acme Spa",
                    "IT123",
                    "info@acme.test",
                    "123",
                    "pec@acme.test",
                    "Via Roma 1",
                    "Roma",
                    "00100",
                    "IT",
                    CustomerStatus.ACTIVE
            );
            Customer customer = mock(Customer.class);
            when(customer.getId()).thenReturn(10L);
            when(customer.getDisplayName()).thenReturn("Acme Spa");
            when(customer.getLegalName()).thenReturn("Acme Spa");
            when(customer.getVatNumber()).thenReturn("IT123");
            when(customer.getPec()).thenReturn("pec@acme.test");
            when(customer.getCountry()).thenReturn("IT");
            when(customerService.createCustomer(any(CreateCustomerRequest.class), any(AuthPrincipal.class)))
                    .thenReturn(customer);

            mockMvc.perform(post("/manager/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10L))
                    .andExpect(jsonPath("$.displayName").value("Acme Spa"))
                    .andExpect(jsonPath("$.vatNumber").value("IT123"))
                    .andExpect(jsonPath("$.country").value("IT"));
        }

        @Test
        void createCustomerReturnsServerErrorWhenServiceThrows() throws Exception {
            setPrincipal();
            CreateCustomerRequest req = new CreateCustomerRequest(
                    "Acme Spa",
                    "Acme Spa",
                    "IT123",
                    "info@acme.test",
                    "123",
                    "pec@acme.test",
                    "Via Roma 1",
                    "Roma",
                    "00100",
                    "IT",
                    CustomerStatus.ACTIVE
            );
            when(customerService.createCustomer(any(CreateCustomerRequest.class), any(AuthPrincipal.class)))
                    .thenThrow(new RuntimeException("boom"));

            mockMvc.perform(post("/manager/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Internal error"));
        }
    }

    @Nested
    class getCustomerTests {
        @Test
        void getCustomerReturnsCustomerWhenFound() throws Exception {
            setPrincipal();
            Customer customer = mock(Customer.class);
            when(customer.getId()).thenReturn(10L);
            when(customer.getDisplayName()).thenReturn("Acme Spa");
            when(customer.getLegalName()).thenReturn("Acme Spa");
            when(customer.getStatus()).thenReturn(CustomerStatus.ACTIVE);
            when(customer.getEmail()).thenReturn("info@acme.test");
            when(customer.getPhone()).thenReturn("123");
            when(customer.getVatNumber()).thenReturn("IT123");
            when(customer.getPec()).thenReturn("pec@acme.test");
            when(customer.getAddressLine1()).thenReturn("Via Roma 1");
            when(customer.getCity()).thenReturn("Roma");
            when(customer.getPostalCode()).thenReturn("00100");
            when(customer.getCountry()).thenReturn("IT");
            when(customerService.getCustomer(eq(10L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.of(customer));

            mockMvc.perform(get("/manager/customers/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10L))
                    .andExpect(jsonPath("$.displayName").value("Acme Spa"))
                    .andExpect(jsonPath("$.vatNumber").value("IT123"))
                    .andExpect(jsonPath("$.country").value("IT"));
        }

        @Test
        void getCustomerReturnsNotFoundWhenMissing() throws Exception {
            setPrincipal();
            when(customerService.getCustomer(eq(10L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/manager/customers/10"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class updateCustomerTests {
        @Test
        void updateCustomerReturnsCustomerWhenValid() throws Exception {
            setPrincipal();
            UpdateCustomerRequest req = new UpdateCustomerRequest(
                    "Acme Spa",
                    "Acme Spa",
                    "IT123",
                    "info@acme.test",
                    "123",
                    "pec@acme.test",
                    "Via Roma 1",
                    "Roma",
                    "00100",
                    "IT"
            );
            Customer customer = mock(Customer.class);
            when(customer.getId()).thenReturn(10L);
            when(customer.getDisplayName()).thenReturn("Acme Spa");
            when(customer.getLegalName()).thenReturn("Acme Spa");
            when(customer.getStatus()).thenReturn(CustomerStatus.ACTIVE);
            when(customer.getEmail()).thenReturn("info@acme.test");
            when(customer.getPhone()).thenReturn("123");
            when(customer.getVatNumber()).thenReturn("IT123");
            when(customer.getPec()).thenReturn("pec@acme.test");
            when(customer.getAddressLine1()).thenReturn("Via Roma 1");
            when(customer.getCity()).thenReturn("Roma");
            when(customer.getPostalCode()).thenReturn("00100");
            when(customer.getCountry()).thenReturn("IT");
            when(customerService.updateCustomer(eq(10L), any(UpdateCustomerRequest.class), any(AuthPrincipal.class)))
                    .thenReturn(customer);

            mockMvc.perform(patch("/manager/customers/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10L))
                    .andExpect(jsonPath("$.displayName").value("Acme Spa"))
                    .andExpect(jsonPath("$.vatNumber").value("IT123"))
                    .andExpect(jsonPath("$.country").value("IT"));
        }

        @Test
        void updateCustomerReturnsServerErrorWhenServiceThrows() throws Exception {
            setPrincipal();
            UpdateCustomerRequest req = new UpdateCustomerRequest(
                    "Acme Spa",
                    "Acme Spa",
                    "IT123",
                    "info@acme.test",
                    "123",
                    "pec@acme.test",
                    "Via Roma 1",
                    "Roma",
                    "00100",
                    "IT"
            );
            when(customerService.updateCustomer(eq(10L), any(UpdateCustomerRequest.class), any(AuthPrincipal.class)))
                    .thenThrow(new RuntimeException("boom"));

            mockMvc.perform(patch("/manager/customers/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Internal error"));
        }
    }

    @Nested
    class deleteCustomerTests {
        @Test
        void deleteCustomerReturnsNoContentWhenDeleted() throws Exception {
            setPrincipal();
            when(customerService.deleteCustomer(eq(10L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.of(Boolean.TRUE));

            mockMvc.perform(delete("/manager/customers/10"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void deleteCustomerReturnsNotFoundWhenMissing() throws Exception {
            setPrincipal();
            when(customerService.deleteCustomer(eq(10L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(delete("/manager/customers/10"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class listCustomersSummaryTests {
        @Test
        void listCustomersSummaryReturnsPageWhenNotEmpty() throws Exception {
            setPrincipal();
            Customer customer = mock(Customer.class);
            when(customer.getId()).thenReturn(10L);
            when(customer.getDisplayName()).thenReturn("Acme Spa");
            when(customer.getLegalName()).thenReturn("Acme Spa");
            when(customer.getVatNumber()).thenReturn("IT123");
            when(customer.getPec()).thenReturn("pec@acme.test");
            when(customer.getCountry()).thenReturn("IT");
            Page<Customer> page = new PageImpl<>(List.of(customer));
            when(customerService.listCustomers(any(AuthPrincipal.class), eq(0), eq(20), eq("displayName,asc"), eq(null)))
                    .thenReturn(page);

            mockMvc.perform(get("/manager/customers")
                            .param("type", "summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(10L))
                    .andExpect(jsonPath("$.content[0].displayName").value("Acme Spa"))
                    .andExpect(jsonPath("$.content[0].vatNumber").value("IT123"))
                    .andExpect(jsonPath("$.content[0].country").value("IT"));
        }

        @Test
        void listCustomersSummaryReturnsNoContentWhenEmpty() throws Exception {
            setPrincipal();
            Page<Customer> page = Page.empty();
            when(customerService.listCustomers(any(AuthPrincipal.class), eq(0), eq(20), eq("displayName,asc"), eq(null)))
                    .thenReturn(page);

            mockMvc.perform(get("/manager/customers")
                            .param("type", "summary"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    class listCustomersTests {
        @Test
        void listCustomersReturnsPageWhenNotEmpty() throws Exception {
            setPrincipal();
            Customer customer = mock(Customer.class);
            when(customer.getId()).thenReturn(10L);
            when(customer.getDisplayName()).thenReturn("Acme Spa");
            when(customer.getLegalName()).thenReturn("Acme Spa");
            when(customer.getStatus()).thenReturn(CustomerStatus.ACTIVE);
            when(customer.getEmail()).thenReturn("info@acme.test");
            when(customer.getPhone()).thenReturn("123");
            when(customer.getVatNumber()).thenReturn("IT123");
            when(customer.getPec()).thenReturn("pec@acme.test");
            when(customer.getAddressLine1()).thenReturn("Via Roma 1");
            when(customer.getCity()).thenReturn("Roma");
            when(customer.getPostalCode()).thenReturn("00100");
            when(customer.getCountry()).thenReturn("IT");
            Page<Customer> page = new PageImpl<>(List.of(customer));
            when(customerService.listCustomers(any(AuthPrincipal.class), eq(0), eq(20), eq("displayName,asc"), eq(null)))
                    .thenReturn(page);

            mockMvc.perform(get("/manager/customers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(10L))
                    .andExpect(jsonPath("$.content[0].displayName").value("Acme Spa"))
                    .andExpect(jsonPath("$.content[0].vatNumber").value("IT123"))
                    .andExpect(jsonPath("$.content[0].country").value("IT"));
        }

        @Test
        void listCustomersReturnsNoContentWhenEmpty() throws Exception {
            setPrincipal();
            Page<Customer> page = Page.empty();
            when(customerService.listCustomers(any(AuthPrincipal.class), eq(0), eq(20), eq("displayName,asc"), eq(null)))
                    .thenReturn(page);

            mockMvc.perform(get("/manager/customers"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    class archiveCustomerTests {
        @Test
        void archiveCustomerReturnsNoContentWhenArchived() throws Exception {
            setPrincipal();
            when(customerService.archiveCustomer(eq(10L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.of(Boolean.TRUE));

            mockMvc.perform(post("/manager/customers/10/archive"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void archiveCustomerReturnsNotFoundWhenMissing() throws Exception {
            setPrincipal();
            when(customerService.archiveCustomer(eq(10L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(post("/manager/customers/10/archive"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class restoreCustomerTests {
        @Test
        void restoreCustomerReturnsNoContentWhenRestored() throws Exception {
            setPrincipal();
            when(customerService.restoreCustomer(eq(10L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.of(Boolean.TRUE));

            mockMvc.perform(post("/manager/customers/10/restore"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void restoreCustomerReturnsNotFoundWhenMissing() throws Exception {
            setPrincipal();
            when(customerService.restoreCustomer(eq(10L), any(AuthPrincipal.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(post("/manager/customers/10/restore"))
                    .andExpect(status().isNotFound());
        }
    }
}
