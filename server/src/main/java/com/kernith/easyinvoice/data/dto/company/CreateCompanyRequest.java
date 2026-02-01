package com.kernith.easyinvoice.data.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 32) String vatNumber
) {}