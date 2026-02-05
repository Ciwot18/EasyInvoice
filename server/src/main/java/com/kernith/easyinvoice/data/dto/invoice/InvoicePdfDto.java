package com.kernith.easyinvoice.data.dto.invoice;

import java.time.LocalDateTime;

public record InvoicePdfDto(
        Long id,
        String fileName,
        LocalDateTime createdAt
) {}