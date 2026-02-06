package com.kernith.easyinvoice.data.dto.dashboard;

public record AdminDashboardSummaryResponse(
        long companies,
        long users,
        long enabledUsers,
        long disabledUsers
) {}
