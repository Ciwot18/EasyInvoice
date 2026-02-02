package com.kernith.easyinvoice.data.dto.customer;

import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.CustomerStatus;
import java.time.LocalDateTime;

public record CustomerDetailResponse(
        Long id,
        String displayName,
        String legalName,
        CustomerStatus status,
        String email,
        String phone,
        String vatNumber,
        String pec,
        String addressLine1,
        String city,
        String postalCode,
        String country,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CustomerDetailResponse from(Customer customer) {
        return new CustomerDetailResponse(
                customer.getId(),
                customer.getDisplayName(),
                customer.getLegalName(),
                customer.getStatus(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getVatNumber(),
                customer.getPec(),
                customer.getAddressLine1(),
                customer.getCity(),
                customer.getPostalCode(),
                customer.getCountry(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}