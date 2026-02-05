package com.kernith.easyinvoice.data.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InvoicePdfArchiveTests {

    @Test
    void invoicePdfArchiveGettersWork() {
        Company company = new Company();
        Customer customer = new Customer(company);
        Invoice invoice = new Invoice(company, customer);

        InvoicePdfArchive archive = new InvoicePdfArchive(invoice, "companies/1/invoices/2", "INV_2.pdf");

        assertEquals(invoice, archive.getInvoice());
        assertEquals("companies/1/invoices/2", archive.getPath());
        assertEquals("INV_2.pdf", archive.getFileName());
        assertNull(archive.getId());
        assertNull(archive.getCreatedAt());
    }
}
