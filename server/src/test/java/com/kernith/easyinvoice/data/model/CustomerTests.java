package com.kernith.easyinvoice.data.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomerTests {

    @Test
    void customerDefaultsAndSettersWork() {
        Company company = new Company();
        Customer customer = new Customer(company);

        assertEquals(company, customer.getCompany());
        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());

        customer.setDisplayName("Acme Spa");
        customer.setLegalName("Acme Spa");
        customer.setVatNumber("IT123");
        customer.setPec("pec@acme.test");
        customer.setCountry("IT");
        customer.setStatus(CustomerStatus.ARCHIVED);

        assertEquals("Acme Spa", customer.getDisplayName());
        assertEquals("Acme Spa", customer.getLegalName());
        assertEquals("IT123", customer.getVatNumber());
        assertEquals("pec@acme.test", customer.getPec());
        assertEquals("IT", customer.getCountry());
        assertEquals(CustomerStatus.ARCHIVED, customer.getStatus());
    }

    @Test
    void customerStatusEnumHasExpectedValues() {
        assertNotNull(CustomerStatus.valueOf("ACTIVE"));
        assertNotNull(CustomerStatus.valueOf("ARCHIVED"));
        assertNotNull(CustomerStatus.valueOf("DELETED"));
    }
}
