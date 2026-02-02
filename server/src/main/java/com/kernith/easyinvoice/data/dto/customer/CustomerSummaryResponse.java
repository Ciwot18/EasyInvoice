package com.kernith.easyinvoice.data.dto.customer;

import com.kernith.easyinvoice.data.model.Customer;

public record CustomerSummaryResponse(
        Long id,
        String displayName,
        String legalName,
        String vatNumber,
        String pec,
        String country
) {
    public static CustomerSummaryResponse from(Customer customer) {
        return new CustomerSummaryResponse(
                customer.getId(),
                customer.getDisplayName(),
                customer.getLegalName(),
                customer.getVatNumber(),
                customer.getPec(),
                customer.getCountry()
        );
    }
}