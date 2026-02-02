package com.kernith.easyinvoice.data.dto.company;

import com.kernith.easyinvoice.data.model.Company;

import java.time.LocalDateTime;

public record CompanySummaryResponse(
        Long id,
        String name,
        String vatNumber,
        LocalDateTime createdAt
) {
    public static CompanySummaryResponse from(Company company) {
        return new CompanySummaryResponse(
                company.getId(),
                company.getName(),
                company.getVatNumber(),
                company.getCreatedAt()
        );
    }
}