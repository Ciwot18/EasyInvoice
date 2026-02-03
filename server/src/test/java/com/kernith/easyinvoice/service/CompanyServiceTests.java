package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.company.CompanySummaryResponse;
import com.kernith.easyinvoice.data.dto.company.CreateCompanyManagerRequest;
import com.kernith.easyinvoice.data.dto.company.CreateCompanyRequest;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.User;
import com.kernith.easyinvoice.data.model.UserRole;
import com.kernith.easyinvoice.data.repository.CompanyRepository;
import com.kernith.easyinvoice.data.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class CompanyServiceTests {

    @Test
    void createCompanyReturnsSummaryWhenValid() {
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CompanyService companyService = new CompanyService(companyRepository, userRepository);

        User platformUser = new User(new Company());
        platformUser.setRole(UserRole.PLATFORM_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(platformUser));

        Company company = new Company();
        company.setName("Acme");
        company.setVatNumber("IT123");
        when(companyRepository.save(any(Company.class))).thenReturn(company);

        AuthPrincipal principal = new AuthPrincipal(1L, 1L, "PLATFORM_ADMIN", List.of());
        CompanySummaryResponse res = companyService.createCompany(new CreateCompanyRequest("Acme", "IT123"), principal);

        assertEquals("Acme", res.name());
        assertEquals("IT123", res.vatNumber());
    }

    @Test
    void createCompanyManagerReturnsEmptyWhenCompanyMissing() {
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CompanyService companyService = new CompanyService(companyRepository, userRepository);

        User platformUser = new User(new Company());
        platformUser.setRole(UserRole.PLATFORM_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(platformUser));
        when(companyRepository.findById(10L)).thenReturn(Optional.empty());

        AuthPrincipal principal = new AuthPrincipal(1L, 1L, "PLATFORM_ADMIN", List.of());
        CreateCompanyManagerRequest req = new CreateCompanyManagerRequest("manager@acme.test", "password123");

        Optional<?> result = companyService.createCompanyManager(10L, req, principal);

        assertTrue(result.isEmpty());
    }

    @Test
    void listCompaniesReturnsSummariesWhenAllowed() {
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CompanyService companyService = new CompanyService(companyRepository, userRepository);

        User platformUser = new User(new Company());
        platformUser.setRole(UserRole.PLATFORM_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(platformUser));

        Company company = new Company();
        company.setName("Acme");
        company.setVatNumber("IT123");
        when(companyRepository.findAllByOrderByNameAsc()).thenReturn(List.of(company));

        AuthPrincipal principal = new AuthPrincipal(1L, 1L, "PLATFORM_ADMIN", List.of());
        List<CompanySummaryResponse> res = companyService.listCompanies(principal);

        assertEquals(1, res.size());
        assertEquals("Acme", res.get(0).name());
        assertEquals("IT123", res.get(0).vatNumber());
    }

    @Test
    void getCompanyReturnsEmptyWhenMissing() {
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CompanyService companyService = new CompanyService(companyRepository, userRepository);

        User platformUser = new User(new Company());
        platformUser.setRole(UserRole.PLATFORM_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(platformUser));
        when(companyRepository.findById(10L)).thenReturn(Optional.empty());

        AuthPrincipal principal = new AuthPrincipal(1L, 1L, "PLATFORM_ADMIN", List.of());
        assertTrue(companyService.getCompany(10L, principal).isEmpty());
    }

    @Test
    void createCompanyManagerThrowsWhenEmailAlreadyUsed() {
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CompanyService companyService = new CompanyService(companyRepository, userRepository);

        User platformUser = new User(new Company());
        platformUser.setRole(UserRole.PLATFORM_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(platformUser));

        Company company = new Company();
        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(userRepository.findByCompanyIdAndEmailIgnoreCase(10L, "manager@acme.test"))
                .thenReturn(Optional.of(new User(company)));

        AuthPrincipal principal = new AuthPrincipal(1L, 1L, "PLATFORM_ADMIN", List.of());
        CreateCompanyManagerRequest req = new CreateCompanyManagerRequest("manager@acme.test", "password123");

        assertThrows(ResponseStatusException.class,
                () -> companyService.createCompanyManager(10L, req, principal));
    }

    @Test
    void createCompanyThrowsWhenRoleNotPlatformAdmin() {
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CompanyService companyService = new CompanyService(companyRepository, userRepository);

        User user = new User(new Company());
        user.setRole(UserRole.COMPANY_MANAGER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        AuthPrincipal principal = new AuthPrincipal(2L, 1L, "COMPANY_MANAGER", List.of());
        assertThrows(ResponseStatusException.class,
                () -> companyService.createCompany(new CreateCompanyRequest("Acme", "IT123"), principal));
    }

    @Test
    void listCompaniesThrowsWhenPrincipalMissing() {
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CompanyService companyService = new CompanyService(companyRepository, userRepository);

        assertThrows(ResponseStatusException.class, () -> companyService.listCompanies(null));
        verify(userRepository, never()).findById(any());
    }
}
