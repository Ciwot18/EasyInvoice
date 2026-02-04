package com.kernith.easyinvoice.data.dto.invoice;

import com.kernith.easyinvoice.data.dto.invoiceitem.CreateInvoiceItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record CreateInvoiceRequest(
        @NotNull Long customerId,
        Long sourceQuoteId,
        @Size(max = 200) String title,
        @Size(max = 2000) String notes,
        LocalDate issueDate,
        LocalDate dueDate,
        @Size(max = 3) String currency,
        @NotNull @Size(min = 1) List<@Valid CreateInvoiceItemRequest> items
) {}
