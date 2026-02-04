package com.kernith.easyinvoice.data.dto.quote;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateQuoteRequest(
        @NotNull Long customerId,
        @Size(max = 200) String title,
        @Size(max = 2000) String notes,
        LocalDate issueDate,
        LocalDate validUntil,
        @Size(max = 3) String currency
) {}
