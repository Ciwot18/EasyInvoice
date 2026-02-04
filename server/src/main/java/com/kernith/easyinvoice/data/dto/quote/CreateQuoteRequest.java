package com.kernith.easyinvoice.data.dto.quote;

import com.kernith.easyinvoice.data.dto.quoteitem.CreateQuoteItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record CreateQuoteRequest(
        @NotNull Long customerId,
        @Size(max = 200) String title,
        @Size(max = 2000) String notes,
        LocalDate issueDate,
        LocalDate validUntil,
        @Size(max = 3) String currency,
        @NotNull @Size(min = 1) List<@Valid CreateQuoteItemRequest> items
) {}
