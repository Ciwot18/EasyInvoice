package com.kernith.easyinvoice.data.dto.invoice;

import org.springframework.core.io.Resource;

public record InvoicePdfDownload(
        String fileName,
        Resource resource
) {}