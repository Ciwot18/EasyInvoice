package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.user.CreateBackofficeUserRequest;
import com.kernith.easyinvoice.data.model.User;
import com.kernith.easyinvoice.data.model.UserRole;
import com.kernith.easyinvoice.data.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.kernith.easyinvoice.helper.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * User management use-cases for company managers and back office users.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Creates the service with repository.
     *
     * @param userRepository user repository
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a back office user for the current company.
     *
     * <p>Lifecycle: validate role, load current user, validate email uniqueness,
     * create user, then save.</p>
     *
     * @param request user creation payload
     * @param principal authenticated principal
     * @return saved user or null if current user is missing
     * @throws ResponseStatusException if validation or authorization fails
     */
    public User createBackofficeUser(CreateBackofficeUserRequest request, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER));   //Only company manager can create back_office users
        Optional<User> optionalUser = getRequiredCurrentUser(principal);
        if (optionalUser.isEmpty()) {
            return null;
        }
        User currentUser = optionalUser.get();

        String email = normalizeEmail(request.email());
        String name = normalizeName(request.name());
        Long companyId = currentUser.getCompany().getId();

        userRepository.findByCompanyIdAndEmailIgnoreCase(companyId, email)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already used for this company");
                });

        User user = new User(currentUser.getCompany());
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.BACK_OFFICE);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    /**
     * Lists users for the current company.
     *
     * @param principal authenticated principal
     * @return list of users (empty if current user missing)
     * @throws ResponseStatusException if authorization fails
     */
    public List<User> listCompanyUsers(AuthPrincipal principal) {
        Optional<User> optionalUser = getRequiredCurrentUser(principal);
        if (optionalUser.isEmpty()) {
            return Collections.emptyList();
        }
        User currentUser = optionalUser.get();
        Long companyId = currentUser.getCompany().getId();

        return userRepository.findByCompanyIdOrderByRoleAscEmailAsc(companyId);
    }

    /**
     * Disables a user in the current company.
     *
     * @param userId target user identifier
     * @param principal authenticated principal
     * @return optional result indicating success
     * @throws ResponseStatusException if authorization fails
     */
    public Optional<Boolean> disableUser(Long userId, AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.PLATFORM_ADMIN));  //Also Platform_Admin can block a user if bad behaviour or suspected hack is detected
        Optional<User> optionalUser = getRequiredCurrentUser(principal);
        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }
        User currentUser = optionalUser.get();

        Long companyId = currentUser.getCompany().getId();
        Optional<User> target = userRepository.findByIdAndCompanyId(userId, companyId);
        if (target.isEmpty()) {
            return Optional.empty();
        }

        target.get().setEnabled(false);
        userRepository.save(target.get());
        return Optional.of(Boolean.TRUE);
    }

    /**
     * Returns the current back office user profile.
     *
     * @param principal authenticated principal
     * @return optional user
     */
    public Optional<User> getBackofficeProfile(AuthPrincipal principal) {
        return getRequiredCurrentUser(principal);
    }

    /**
     * Returns the current user profile.
     *
     * @param principal authenticated principal
     * @return optional user
     */
    public Optional<User> getCurrentUser(AuthPrincipal principal) {
        return getRequiredCurrentUser(principal);
    }

    private Optional<User> getRequiredCurrentUser(AuthPrincipal principal) {
        if (principal == null) {
            return Optional.empty();
        }
        return userRepository.findById(principal.userId());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }
}
