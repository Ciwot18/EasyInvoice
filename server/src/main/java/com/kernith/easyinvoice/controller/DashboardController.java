package com.kernith.easyinvoice.controller;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.dashboard.AdminDashboardSummaryResponse;
import com.kernith.easyinvoice.data.dto.dashboard.CustomerInvoiceSummaryResponse;
import com.kernith.easyinvoice.data.dto.dashboard.ManagerDashboardSummaryResponse;
import com.kernith.easyinvoice.helper.CurrentUser;
import com.kernith.easyinvoice.service.DashboardService;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard endpoints for managers and platform admins.
 */
@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Returns the manager dashboard summary for the current company.
     *
     * @param principal authenticated principal
     * @return summary response
     */
    @GetMapping("/manager/dashboard/summary")
    public ResponseEntity<ManagerDashboardSummaryResponse> managerSummary(
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(dashboardService.getManagerSummary(principal));
    }

    /**
     * Returns an invoice summary for a specific customer within the current company.
     *
     * @param customerId customer identifier
     * @param principal authenticated principal
     * @return summary response or {@code 404 Not Found} if customer missing
     */
    @GetMapping("/manager/customers/{customerId}/invoice-summary")
    public ResponseEntity<CustomerInvoiceSummaryResponse> customerInvoiceSummary(
            @PathVariable("customerId") Long customerId,
            @CurrentUser AuthPrincipal principal
    ) {
        Optional<CustomerInvoiceSummaryResponse> response =
                dashboardService.getCustomerInvoiceSummary(customerId, principal);
        if (response.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(response.get());
    }

    /**
     * Returns the admin dashboard summary for platform admins.
     *
     * @param principal authenticated principal
     * @return admin summary
     */
    @GetMapping("/platform/dashboard/summary")
    public ResponseEntity<AdminDashboardSummaryResponse> adminSummary(
            @CurrentUser AuthPrincipal principal
    ) {
        return ResponseEntity.ok(dashboardService.getAdminSummary(principal));
    }
}
