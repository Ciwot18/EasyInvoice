package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.data.dto.auth.LoginRequest;
import com.kernith.easyinvoice.data.dto.auth.LoginResponse;
import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.User;
import com.kernith.easyinvoice.data.model.UserRole;
import com.kernith.easyinvoice.data.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTests {

    @Test
    void loginReturnsResponseWhenValid() {
        UserRepository userRepository = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        AuthService authService = new AuthService(userRepository, jwtService);

        Company company = mock(Company.class);
        when(company.getId()).thenReturn(10L);

        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        when(user.getCompany()).thenReturn(company);
        when(user.getRole()).thenReturn(UserRole.COMPANY_MANAGER);
        when(user.isEnabled()).thenReturn(true);

        String rawPassword = "password123";
        String hashed = new BCryptPasswordEncoder().encode(rawPassword);
        when(user.getPasswordHash()).thenReturn(hashed);
        when(userRepository.findByCompanyIdAndEmailIgnoreCase(10L, "user@acme.test"))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt");

        LoginResponse res = authService.login(new LoginRequest(10L, "user@acme.test", rawPassword));

        assertEquals("jwt", res.token());
        assertEquals(7L, res.userId());
        assertEquals(10L, res.companyId());
        assertEquals("COMPANY_MANAGER", res.role());
    }

    @Test
    void loginThrowsWhenUserDisabled() {
        UserRepository userRepository = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        AuthService authService = new AuthService(userRepository, jwtService);

        User user = mock(User.class);
        when(user.isEnabled()).thenReturn(false);
        when(userRepository.findByCompanyIdAndEmailIgnoreCase(10L, "user@acme.test"))
                .thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class,
                () -> authService.login(new LoginRequest(10L, "user@acme.test", "pw")));
    }
}
