package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.data.dto.auth.LoginRequest;
import com.kernith.easyinvoice.data.dto.auth.LoginResponse;
import com.kernith.easyinvoice.data.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication use-cases for login and token issuance.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService;

    /**
     * Creates the service with required repositories and JWT utilities.
     *
     * @param userRepository user repository
     * @param jwtService JWT service for token generation
     */
    public AuthService(UserRepository userRepository,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /**
     * Validates credentials and issues a JWT for the user.
     *
     * <p>Lifecycle: find user, verify enabled flag, compare password hash,
     * then generate and return the token.</p>
     *
     * @param req login request
     * @return login response with token and identity info
     * @throws RuntimeException if credentials are invalid or the user is disabled
     */
    public LoginResponse login(LoginRequest req) {
        var user = userRepository
                .findByCompanyIdAndEmailIgnoreCase(req.companyId(), req.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.isEnabled()) {
            throw new RuntimeException("User disabled");
        }

        boolean ok = passwordEncoder.matches(req.password(), user.getPasswordHash());
        if (!ok) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                user.getId(),
                user.getCompany().getId(),
                user.getRole().name()
        );
    }
}
