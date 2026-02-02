package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.data.dto.user.CreateBackofficeUserRequest;
import com.kernith.easyinvoice.data.dto.user.ProfileResponse;
import com.kernith.easyinvoice.data.dto.user.UserSummary;
import com.kernith.easyinvoice.data.model.User;
import com.kernith.easyinvoice.data.model.UserRole;
import com.kernith.easyinvoice.data.repository.UserRepository;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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

    public User createBackofficeUser(CreateBackofficeUserRequest request, Principal principal) {
        Optional<User> optionalUser = getRequiredCurrentUser(principal);
        if (optionalUser.isEmpty()) {
            return null;
        }
        User currentUser = optionalUser.get();
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

        return userRepository.save(user);
    }

    public List<User> listCompanyUsers(Principal principal) {
        Optional<User> optionalUser = getRequiredCurrentUser(principal);
        if (optionalUser.isEmpty()) {
            return Collections.emptyList();
        }
        User currentUser = optionalUser.get();
        Long companyId = currentUser.getCompany().getId();

        return userRepository.findByCompanyIdOrderByRoleAscEmailAsc(companyId);
    }

    public Optional<Boolean> disableUser(Long userId, Principal principal) {
        Optional<User> optionalUser = getRequiredCurrentUser(principal);
        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }
        User currentUser = optionalUser.get();
        requireRoles(currentUser, List.of(UserRole.COMPANY_MANAGER, UserRole.PLATFORM_ADMIN));  //Also Platform_Admin can block a user if bad behaviour or suspected hack is detected

        Long companyId = currentUser.getCompany().getId();
        Optional<User> target = userRepository.findByIdAndCompanyId(userId, companyId);
        if (target.isEmpty()) {
            return Optional.empty();
        }

        target.get().setEnabled(false);
        userRepository.save(target.get());
        return Optional.of(Boolean.TRUE);
    }

    public Optional<User> getBackofficeProfile(Principal principal) {
        return getRequiredCurrentUser(principal);
    }

    private Optional<User> getRequiredCurrentUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(principal.getName());
    }

    private void requireRoles(User user, List<UserRole> roles) {
        if (!roles.contains(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}