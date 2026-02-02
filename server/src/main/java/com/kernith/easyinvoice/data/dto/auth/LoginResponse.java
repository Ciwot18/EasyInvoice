package com.kernith.easyinvoice.data.dto.auth;

public record LoginResponse(
        String token,
        long userId,
        long companyId,
        String role
) {}