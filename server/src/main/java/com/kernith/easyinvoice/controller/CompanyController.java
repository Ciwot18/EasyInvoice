package com.kernith.easyinvoice.controller;

import com.kernith.easyinvoice.data.dto.company.CompanyDetailResponse;
import com.kernith.easyinvoice.data.dto.company.CompanySummaryResponse;
import com.kernith.easyinvoice.data.dto.company.CreateCompanyManagerRequest;
import com.kernith.easyinvoice.data.dto.company.CreateCompanyRequest;
import com.kernith.easyinvoice.data.dto.user.UserSummary;
import com.kernith.easyinvoice.service.CompanyService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/api/platform/companies")
    public ResponseEntity<CompanySummaryResponse> createCompany(
            @Valid @RequestBody CreateCompanyRequest request,
            Principal principal
    ) {
        CompanySummaryResponse company = companyService.createCompany(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(company);
    }

    @GetMapping("/api/platform/companies")
    public ResponseEntity<List<CompanySummaryResponse>> listCompanies(Principal principal) {
        List<CompanySummaryResponse> companies = companyService.listCompanies(principal);
        if (companies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/api/platform/companies/{companyId}")
    public ResponseEntity<CompanyDetailResponse> getCompany(
            @PathVariable("companyId") Long companyId,
            Principal principal
    ) {
        return ResponseEntity.ok(companyService.getCompany(companyId, principal));
    }

    @PostMapping("/api/platform/companies/{companyId}/managers")
    public ResponseEntity<UserSummary> createCompanyManager(
            @PathVariable("companyId") Long companyId,
            @Valid @RequestBody CreateCompanyManagerRequest request,
            Principal principal
    ) {
        UserSummary manager = companyService.createCompanyManager(companyId, request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(manager);
    }
}