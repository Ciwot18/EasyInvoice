package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.user.CreateBackofficeUserRequest;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.User;
import com.kernith.easyinvoice.data.model.UserRole;
import com.kernith.easyinvoice.data.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.web.server.ResponseStatusException;

class UserServiceTests {

    @Test
    void createBackofficeUserReturnsNullWhenCurrentUserMissing() {
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        when(userRepository.findById(7L)).thenReturn(Optional.empty());

        CreateBackofficeUserRequest request = new CreateBackofficeUserRequest("new@acme.test", "Mario Monti","password123");
        User created = userService.createBackofficeUser(request, principal);

        assertNull(created);
    }

    @Test
    void createBackofficeUserReturnsSavedUserWhenValid() {
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        Company company = mock(Company.class);
        when(company.getId()).thenReturn(10L);

        User currentUser = new User(company);
        currentUser.setRole(UserRole.COMPANY_MANAGER);

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        when(userRepository.findById(7L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByCompanyIdAndEmailIgnoreCase(eq(10L), eq("new@acme.test")))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateBackofficeUserRequest request = new CreateBackofficeUserRequest("new@acme.test", "Mario Monti","password123");
        User created = userService.createBackofficeUser(request, principal);

        assertEquals("new@acme.test", created.getEmail());
        assertEquals(UserRole.BACK_OFFICE, created.getRole());
        assertTrue(created.isEnabled());
    }

    @Test
    void listCompanyUsersReturnsEmptyWhenPrincipalNull() {
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        List<User> users = userService.listCompanyUsers(null);

        assertTrue(users.isEmpty());
    }

    @Test
    void listCompanyUsersReturnsUsersWhenCurrentUserFound() {
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        Company company = mock(Company.class);
        when(company.getId()).thenReturn(10L);
        User currentUser = new User(company);

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        when(userRepository.findById(7L)).thenReturn(Optional.of(currentUser));

        List<User> expected = new ArrayList<>();
        expected.add(new User(company));
        when(userRepository.findByCompanyIdOrderByRoleAscEmailAsc(10L)).thenReturn(expected);

        List<User> users = userService.listCompanyUsers(principal);

        assertEquals(1, users.size());
    }

    @Test
    void listPlatformUsersReturnsUsersForPlatformAdmin() {
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        Company company = mock(Company.class);
        List<User> expected = List.of(new User(company));
        AuthPrincipal principal = new AuthPrincipal(1L, 1L, "PLATFORM_ADMIN", List.of());

        when(userRepository.findAllByOrderByCompanyIdAscRoleAscEmailAsc()).thenReturn(expected);

        List<User> users = userService.listPlatformUsers(principal);

        assertEquals(1, users.size());
    }

    @Test
    void listPlatformUsersThrowsWhenRoleIsNotPlatformAdmin() {
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);
        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());

        assertThrows(ResponseStatusException.class, () -> userService.listPlatformUsers(principal));
    }

    @Test
    void disableUserReturnsEmptyWhenTargetMissing() {
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        Company company = mock(Company.class);
        when(company.getId()).thenReturn(10L);
        User currentUser = new User(company);
        currentUser.setRole(UserRole.COMPANY_MANAGER);

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        when(userRepository.findById(7L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByIdAndCompanyId(99L, 10L)).thenReturn(Optional.empty());

        Optional<Boolean> result = userService.disableUser(99L, principal);

        assertTrue(result.isEmpty());
    }

    @Test
    void disableUserDisablesTargetWhenFound() {
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        Company company = mock(Company.class);
        when(company.getId()).thenReturn(10L);
        User currentUser = new User(company);
        currentUser.setRole(UserRole.COMPANY_MANAGER);

        User target = new User(company);
        target.setEnabled(true);

        AuthPrincipal principal = new AuthPrincipal(7L, 10L, "COMPANY_MANAGER", List.of());
        when(userRepository.findById(7L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByIdAndCompanyId(99L, 10L)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Boolean> result = userService.disableUser(99L, principal);

        assertTrue(result.isPresent());
        assertTrue(!target.isEnabled());
    }
}
