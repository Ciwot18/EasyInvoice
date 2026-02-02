package com.kernith.easyinvoice.data.dto.company;

import com.kernith.easyinvoice.data.model.Company;

import java.time.LocalDateTime;

public record CompanyDetailResponse(
        Long id,
        String name,
        String vatNumber,
        LocalDateTime createdAt
) {
    public static CompanyDetailResponse from (Company company) {
        return new CompanyDetailResponse(
                company.getId(),
                company.getName(),
                company.getVatNumber(),
                company.getCreatedAt()
        );
    }
}