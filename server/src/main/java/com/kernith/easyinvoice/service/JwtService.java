package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.data.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Issues and parses JWT tokens for API authentication.
 */
@Service
public class JwtService {
    private final String secret;
    private final long expMinutes;

    /**
     * Creates the service with the shared secret and expiration policy.
     *
     * @param secret signing key
     * @param expMinutes token expiration in minutes
     */
    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.exp-minutes}") long expMinutes
    ) {
        this.secret = secret;
        this.expMinutes = expMinutes;
    }

    /**
     * Generates a signed JWT for the given user.
     *
     * <p>Lifecycle: compute issue/expiry timestamps, set subject and claims,
     * sign with HMAC, then return the compact token.</p>
     *
     * @param user authenticated user
     * @return signed JWT string
     */
    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(expMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("cid", user.getCompany().getId())
                .claim("role", user.getRole().name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    /**
     * Parses and validates a JWT, returning its claims.
     *
     * @param token JWT string
     * @return parsed claims
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
