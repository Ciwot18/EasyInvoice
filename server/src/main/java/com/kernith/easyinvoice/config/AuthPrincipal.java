package com.kernith.easyinvoice.config;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

/**
 * Authenticated principal stored in Spring Security context.
 *
 * <p>It mirrors the JWT claims and exposes the user and company identifiers,
 * the raw role string, and the granted authorities used by the framework.</p>
 *
 * @param userId authenticated user id
 * @param companyId authenticated company id
 * @param role role name from the token (without {@code ROLE_} prefix)
 * @param authorities granted authorities derived from the role
 */
public record AuthPrincipal(
        long userId,
        long companyId,
        String role,
        Collection<? extends GrantedAuthority> authorities
) {}
