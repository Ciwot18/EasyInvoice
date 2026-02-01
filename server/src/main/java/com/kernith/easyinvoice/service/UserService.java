package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.data.dto.user.CreateBackofficeUserRequest;
import com.kernith.easyinvoice.data.dto.user.ProfileResponse;
import com.kernith.easyinvoice.data.dto.user.UserSummary;
import com.kernith.easyinvoice.data.model.User;
import com.kernith.easyinvoice.data.model.UserRole;
import com.kernith.easyinvoice.data.repository.UserRepository;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserSummary createBackofficeUser(CreateBackofficeUserRequest request, Principal principal) {
        User currentUser = getRequiredCurrentUser(principal);
        requireRoles(currentUser, List.of(UserRole.COMPANY_MANAGER));   //Only company manager can create back_office users

        String email = normalizeEmail(request.email());
        Long companyId = currentUser.getCompany().getId();

        userRepository.findByCompanyIdAndEmailIgnoreCase(companyId, email)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already used for this company");
                });

        User user = new User(currentUser.getCompany());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.BACK_OFFICE);
        user.setEnabled(true);

        User saved = userRepository.save(user);
        return toSummary(saved);
    }

    public List<UserSummary> listCompanyUsers(Principal principal) {
        User currentUser = getRequiredCurrentUser(principal);
        Long companyId = currentUser.getCompany().getId();

        return userRepository.findByCompanyIdOrderByRoleAndEmailAsc(companyId)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public void disableUser(Long userId, Principal principal) {
        User currentUser = getRequiredCurrentUser(principal);
        requireRoles(currentUser, List.of(UserRole.COMPANY_MANAGER, UserRole.PLATFORM_ADMIN));  //Also Platform_Admin can block a user if bad behaviour or suspected hack is detected

        Long companyId = currentUser.getCompany().getId();
        User target = userRepository.findByIdAndCompanyId(userId, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        target.setEnabled(false);
        userRepository.save(target);
    }

    public ProfileResponse getBackofficeProfile(Principal principal) {
        User currentUser = getRequiredCurrentUser(principal);
        return toProfile(currentUser);
    }

    private User getRequiredCurrentUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing principal");
        }

        return userRepository.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private void requireRoles(User user, List<UserRole> roles) {
        if (!roles.contains(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private UserSummary toSummary(User user) {
        return new UserSummary(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled()
        );
    }

    private ProfileResponse toProfile(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}