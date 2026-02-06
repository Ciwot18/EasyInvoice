package com.kernith.easyinvoice.data.dto.dashboard;

/**
 * Summary payload for platform admin dashboard totals and system stats.
 *
 * @param companies number of companies
 * @param users total users
 * @param enabledUsers enabled users count
 * @param disabledUsers disabled users count
 * @param diskTotalBytes total bytes for the selected filesystem
 * @param diskFreeBytes available bytes for the selected filesystem
 * @param ramTotalBytes total physical memory in bytes
 * @param ramFreeBytes free physical memory in bytes
 * @param diskPath path used to resolve the filesystem stats
 * @param dbFileBytes database file size in bytes (null when in-memory or unavailable)
 * @param dbPath database file path or descriptor (e.g. "in-memory")
 */
public record AdminDashboardSummaryResponse(
        long companies,
        long users,
        long enabledUsers,
        long disabledUsers,
        Long diskTotalBytes,
        Long diskFreeBytes,
        Long ramTotalBytes,
        Long ramFreeBytes,
        String diskPath,
        Long dbFileBytes,
        String dbPath
) {
    public static AdminDashboardSummaryResponse from(
            long companies,
            long users,
            long enabledUsers,
            long disabledUsers,
            Long diskTotalBytes,
            Long diskFreeBytes,
            Long ramTotalBytes,
            Long ramFreeBytes,
            String diskPath,
            Long dbFileBytes,
            String dbPath
    ) {
        return new AdminDashboardSummaryResponse(
                companies,
                users,
                enabledUsers,
                disabledUsers,
                diskTotalBytes,
                diskFreeBytes,
                ramTotalBytes,
                ramFreeBytes,
                diskPath,
                dbFileBytes,
                dbPath
        );
    }
}
