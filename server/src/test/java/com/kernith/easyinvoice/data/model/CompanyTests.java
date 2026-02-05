package com.kernith.easyinvoice.data.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CompanyTests {

    @Test
    void companySettersAndGettersWork() {
        Company company = new Company();
        assertNull(company.getId());
        assertNull(company.getCreatedAt());
        assertNull(company.getAddress());

        company.setName("Acme");
        company.setVatNumber("IT123");
        company.setAddress("Via Roma 1");

        assertEquals("Acme", company.getName());
        assertEquals("IT123", company.getVatNumber());
        assertEquals("Via Roma 1", company.getAddress());
    }
}
