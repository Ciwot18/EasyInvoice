package com.kernith.easyinvoice.controller;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.company.CompanyDetailResponse;
import com.kernith.easyinvoice.data.dto.company.CompanySummaryResponse;
import com.kernith.easyinvoice.data.dto.company.CreateCompanyManagerRequest;
import com.kernith.easyinvoice.data.dto.company.CreateCompanyRequest;
import com.kernith.easyinvoice.data.dto.user.UserSummary;
import com.kernith.easyinvoice.helper.CurrentUser;
import com.kernith.easyinvoice.service.CompanyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Platform admin endpoints for company management.
 */
@RestController
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * Creates a new company.
     *
     * @param request company creation payload
     * @param principal authenticated principal
     * @return created company summary
     * @throws org.springframework.web.server.ResponseStatusException if validation or authorization fails
     */
    @PostMapping("/platform/companies")
    public ResponseEntity<CompanySummaryResponse> createCompany(
            @Valid @RequestBody CreateCompanyRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        CompanySummaryResponse company = companyService.createCompany(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(company);
    }

    /**
     * Lists all companies.
     *
     * @param principal authenticated principal
     * @return list of company summaries or {@code 204 No Content} if empty
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @GetMapping("/platform/companies")
    public ResponseEntity<List<CompanySummaryResponse>> listCompanies(@CurrentUser AuthPrincipal principal) {
        List<CompanySummaryResponse> companies = companyService.listCompanies(principal);
        if (companies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(companies);
    }

    /**
     * Returns company details by id.
     *
     * @param companyId company identifier
     * @param principal authenticated principal
     * @return company details or {@code 404 Not Found} if missing
     * @throws org.springframework.web.server.ResponseStatusException if authorization fails
     */
    @GetMapping("/platform/companies/{companyId}")
    public ResponseEntity<CompanyDetailResponse> getCompany(
            @PathVariable("companyId") Long companyId,
            @CurrentUser AuthPrincipal principal
    ) {
        return companyService.getCompany(companyId, principal)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a company manager for a given company.
     *
     * @param companyId company identifier
     * @param request manager creation payload
     * @param principal authenticated principal
     * @return created manager summary or {@code 404 Not Found} if company missing
     * @throws org.springframework.web.server.ResponseStatusException if validation or authorization fails
     */
    @PostMapping("/platform/companies/{companyId}/managers")
    public ResponseEntity<UserSummary> createCompanyManager(
            @PathVariable("companyId") Long companyId,
            @Valid @RequestBody CreateCompanyManagerRequest request,
            @CurrentUser AuthPrincipal principal
    ) {
        return companyService.createCompanyManager(companyId, request, principal)
                .map(manager -> ResponseEntity.status(HttpStatus.CREATED).body(manager))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
