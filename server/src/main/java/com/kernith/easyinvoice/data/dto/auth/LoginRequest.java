package com.kernith.easyinvoice.data.dto.auth;

public record LoginRequest(
        Long companyId,
        String email,
        String password
) {}