package com.kernith.easyinvoice.config;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public record AuthPrincipal(
        long userId,
        long companyId,
        String role,
        Collection<? extends GrantedAuthority> authorities
) {}