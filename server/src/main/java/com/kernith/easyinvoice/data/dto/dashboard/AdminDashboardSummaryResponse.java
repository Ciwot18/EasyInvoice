package com.kernith.easyinvoice.data.dto.dashboard;

/**
 * Summary payload for platform admin dashboard totals.
 *
 * @param companies number of companies
 * @param users total users
 * @param enabledUsers enabled users count
 * @param disabledUsers disabled users count
 */
public record AdminDashboardSummaryResponse(
        long companies,
        long users,
        long enabledUsers,
        long disabledUsers
) {}
