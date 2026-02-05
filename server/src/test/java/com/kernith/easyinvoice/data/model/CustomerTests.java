package com.kernith.easyinvoice.data.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CustomerTests {

    @Test
    void customerDefaultsAndSettersWork() {
        Company company = new Company();
        Customer customer = new Customer(company);

        assertEquals(company, customer.getCompany());
        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
        assertNull(customer.getId());

        customer.setDisplayName("Acme Spa");
        customer.setLegalName("Acme Spa");
        customer.setVatNumber("IT123");
        customer.setPec("pec@acme.test");
        customer.setCountry("IT");
        customer.setEmail("info@acme.test");
        customer.setPhone("123");
        customer.setAddress("Via Roma 1");
        customer.setCity("Roma");
        customer.setPostalCode("00100");
        customer.setStatus(CustomerStatus.ARCHIVED);

        assertEquals("Acme Spa", customer.getDisplayName());
        assertEquals("Acme Spa", customer.getLegalName());
        assertEquals("IT123", customer.getVatNumber());
        assertEquals("pec@acme.test", customer.getPec());
        assertEquals("IT", customer.getCountry());
        assertEquals("info@acme.test", customer.getEmail());
        assertEquals("123", customer.getPhone());
        assertEquals("Via Roma 1", customer.getAddress());
        assertEquals("Roma", customer.getCity());
        assertEquals("00100", customer.getPostalCode());
        assertEquals(CustomerStatus.ARCHIVED, customer.getStatus());
        assertNull(customer.getCreatedAt());
        assertNull(customer.getUpdatedAt());
    }

    @Test
    void customerStatusEnumHasExpectedValues() {
        assertNotNull(CustomerStatus.valueOf("ACTIVE"));
        assertNotNull(CustomerStatus.valueOf("ARCHIVED"));
        assertNotNull(CustomerStatus.valueOf("DELETED"));
    }
}
