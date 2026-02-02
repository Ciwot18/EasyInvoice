package com.kernith.easyinvoice.data.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @Size(max = 160) String displayName,
        @Size(max = 200) String legalName,
        @Size(max = 20) String vatNumber,
        @Email @Size(max = 254) String email,
        @Size(max = 40) String phone,
        @Email @Size(max = 254) String pec,
        @Size(max = 200) String addressLine1,
        @Size(max = 120) String city,
        @Size(max = 20) String postalCode,
        @Size(max = 2) String country
) {}