package com.kernith.easyinvoice.config;

import com.kernith.easyinvoice.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Con context-path /api le richieste avranno path "/auth/login"
        String path = request.getRequestURI();
        return path.startsWith("/auth/") || path.startsWith("/error");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {  // Faccio passare al Security
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length()).trim();

        try {
            Claims claims = jwtService.parseClaims(token);

            long userId = Long.parseLong(claims.getSubject());
            long companyId = ((Number) claims.get("cid")).longValue();
            String role = String.valueOf(claims.get("role"));
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            var principal = new AuthPrincipal(userId, companyId, role, authorities);

            var authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ex) {
            // Con token invalido/scaduto pulisco contesto e continuo
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}