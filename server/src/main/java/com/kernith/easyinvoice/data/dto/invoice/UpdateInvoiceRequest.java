package com.kernith.easyinvoice.data.dto.invoice;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateInvoiceRequest(
        @Size(max = 200) String title,
        @Size(max = 2000) String notes,
        LocalDate issueDate,
        LocalDate dueDate,
        @Size(max = 3) String currency
) {}
