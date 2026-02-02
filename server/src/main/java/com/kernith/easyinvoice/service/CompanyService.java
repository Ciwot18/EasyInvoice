package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.data.dto.company.CompanyDetailResponse;
import com.kernith.easyinvoice.data.dto.company.CompanySummaryResponse;
import com.kernith.easyinvoice.data.dto.company.CreateCompanyManagerRequest;
import com.kernith.easyinvoice.data.dto.company.CreateCompanyRequest;
import com.kernith.easyinvoice.data.dto.user.UserSummary;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.User;
import com.kernith.easyinvoice.data.model.UserRole;
import com.kernith.easyinvoice.data.repository.CompanyRepository;
import com.kernith.easyinvoice.data.repository.UserRepository;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public CompanySummaryResponse createCompany(CreateCompanyRequest request, Principal principal) {
        requirePlatformAdmin(principal);

        Company company = new Company();
        company.setName(request.name().trim());
        company.setVatNumber(normalizeVat(request.vatNumber()));

        Company saved = companyRepository.save(company);
        return toSummary(saved);
    }

    public List<CompanySummaryResponse> listCompanies(Principal principal) {
        requirePlatformAdmin(principal);

        return companyRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public Optional<CompanyDetailResponse> getCompany(Long companyId, Principal principal) {
        requirePlatformAdmin(principal);

        return companyRepository.findById(companyId)
                .map(this::toDetail);
    }

    public Optional<UserSummary> createCompanyManager(Long companyId, CreateCompanyManagerRequest request, Principal principal) {
        requirePlatformAdmin(principal);

        Optional<Company> company = companyRepository.findById(companyId);
        if (company.isEmpty()) {
            return Optional.empty();
        }

        String email = normalizeEmail(request.email());
        userRepository.findByCompanyIdAndEmailIgnoreCase(companyId, email)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already used for this company");
                });

        User user = new User(company.get());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.COMPANY_MANAGER);
        user.setEnabled(true);

        User saved = userRepository.save(user);
        return Optional.of(new UserSummary(saved.getId(), saved.getEmail(), saved.getRole(), saved.isEnabled()));
    }

    private void requirePlatformAdmin(Principal principal) {
        User currentUser = getRequiredCurrentUser(principal);
        if (currentUser.getRole() != UserRole.PLATFORM_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role");
        }
    }

    private User getRequiredCurrentUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing principal");
        }

        return userRepository.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeVat(String vatNumber) {
        if (vatNumber == null) {
            return null;
        }
        String value = vatNumber.trim();
        return value.isEmpty() ? null : value;
    }

    private CompanySummaryResponse toSummary(Company company) {
        return new CompanySummaryResponse(
                company.getId(),
                company.getName(),
                company.getVatNumber(),
                company.getCreatedAt()
        );
    }

    private CompanyDetailResponse toDetail(Company company) {
        return new CompanyDetailResponse(
                company.getId(),
                company.getName(),
                company.getVatNumber(),
                company.getCreatedAt()
        );
    }
}
