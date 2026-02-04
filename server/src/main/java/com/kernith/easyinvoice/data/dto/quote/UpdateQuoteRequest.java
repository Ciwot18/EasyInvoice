package com.kernith.easyinvoice.data.dto.quote;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateQuoteRequest(
        @Size(max = 200) String title,
        @Size(max = 2000) String notes,
        LocalDate issueDate,
        LocalDate validUntil,
        @Size(max = 3) String currency
) {}
