package com.kernith.easyinvoice.data.dto.company;

import java.time.LocalDateTime;

public record CompanyDetailResponse(
        Long id,
        String name,
        String vatNumber,
        LocalDateTime createdAt
) {}