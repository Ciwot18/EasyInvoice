package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Platform-level company management use-cases.
 */
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Creates the service with repositories.
     *
     * @param companyRepository company repository
     * @param userRepository user repository
     */
    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a company as a platform admin.
     *
     * <p>Lifecycle: validate platform admin, map fields, save company.</p>
     *
     * @param request company creation payload
     * @param principal authenticated principal
     * @return saved company summary
     * @throws ResponseStatusException if authorization fails
     */
    public CompanySummaryResponse createCompany(CreateCompanyRequest request, AuthPrincipal principal) {
        requirePlatformAdmin(principal);

        Company company = new Company();
        company.setName(request.name().trim());
        company.setVatNumber(normalizeVat(request.vatNumber()));

        Company saved = companyRepository.save(company);
        return CompanySummaryResponse.from(saved);
    }

    /**
     * Lists all companies as a platform admin.
     *
     * @param principal authenticated principal
     * @return list of company summaries
     * @throws ResponseStatusException if authorization fails
     */
    public List<CompanySummaryResponse> listCompanies(AuthPrincipal principal) {
        requirePlatformAdmin(principal);

        return companyRepository.findAllByOrderByNameAsc()
                .stream()
                .map(CompanySummaryResponse::from)
                .toList();
    }

    /**
     * Retrieves company details by id.
     *
     * @param companyId company identifier
     * @param principal authenticated principal
     * @return optional company details
     * @throws ResponseStatusException if authorization fails
     */
    public Optional<CompanyDetailResponse> getCompany(Long companyId, AuthPrincipal principal) {
        requirePlatformAdmin(principal);

        return companyRepository.findById(companyId)
                .map(CompanyDetailResponse::from);
    }

    /**
     * Creates a company manager for a given company.
     *
     * <p>Lifecycle: validate platform admin, ensure company exists, validate email uniqueness,
     * create user with manager role, then save.</p>
     *
     * @param companyId company identifier
     * @param request manager creation payload
     * @param principal authenticated principal
     * @return optional manager summary
     * @throws ResponseStatusException if validation or authorization fails
     */
    public Optional<UserSummary> createCompanyManager(Long companyId, CreateCompanyManagerRequest request, AuthPrincipal principal) {
        requirePlatformAdmin(principal);

        Optional<Company> company = companyRepository.findById(companyId);
        if (company.isEmpty()) {
            return Optional.empty();
        }

        String email = normalizeEmail(request.email());
        String name = normalizeName(request.name());
        userRepository.findByCompanyIdAndEmailIgnoreCase(companyId, email)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already used for this company");
                });

        User user = new User(company.get());
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.COMPANY_MANAGER);
        user.setEnabled(true);

        User saved = userRepository.save(user);
        return Optional.of(UserSummary.from(saved));
    }

    private void requirePlatformAdmin(AuthPrincipal principal) {
        User currentUser = getRequiredCurrentUser(principal);
        if (currentUser.getRole() != UserRole.PLATFORM_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role");
        }
    }

    private User getRequiredCurrentUser(AuthPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing principal");
        }

        return userRepository.findById(principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private String normalizeVat(String vatNumber) {
        if (vatNumber == null) {
            return null;
        }
        String value = vatNumber.trim();
        return value.isEmpty() ? null : value;
    }
}
