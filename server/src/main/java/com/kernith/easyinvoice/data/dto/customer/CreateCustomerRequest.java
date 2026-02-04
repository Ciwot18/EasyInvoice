package com.kernith.easyinvoice.data.dto.customer;

import com.kernith.easyinvoice.data.model.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank @Size(max = 160) String displayName,
        @Size(max = 200) String legalName,
        @NotBlank @Size(max = 20) String vatNumber,
        @Email @Size(max = 254) String email,
        @Size(max = 40) String phone,
        @Email @Size(max = 254) String pec,
        @Size(max = 200) String address,
        @Size(max = 120) String city,
        @Size(max = 20) String postalCode,
        @Size(max = 2) String country,
        CustomerStatus status
) {}