package com.kernith.easyinvoice.config;

import com.kernith.easyinvoice.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Central Spring Security configuration for the application.
 */
@Configuration
public class SecurityConfig {

    /**
     * Builds the security filter chain with JWT authentication and role-based access rules.
     *
     * <p>CSRF is disabled because the API is stateless and uses JWTs. The session is
     * configured as stateless, and only specific paths are public.</p>
     *
     * @param http Spring Security HTTP builder
     * @param jwtService service used by the JWT filter to parse tokens
     * @return configured security filter chain
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        var jwtFilter = new JwtAuthenticationFilter(jwtService);

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/auth/**", "/error").permitAll()
                        .requestMatchers("/invoices/**", "/quotes/**").authenticated()
                        .requestMatchers("/platform/**").hasRole("PLATFORM_ADMIN")
                        .requestMatchers("/manager/**").hasRole("COMPANY_MANAGER")
                        .requestMatchers("/backoffice/**").hasAnyRole("BACK_OFFICE", "COMPANY_MANAGER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .httpBasic(hb -> hb.disable())
                .formLogin(fl -> fl.disable())
                .build();
    }
}
