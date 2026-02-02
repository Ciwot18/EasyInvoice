package com.kernith.easyinvoice.config;

import com.kernith.easyinvoice.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        var jwtFilter = new JwtAuthenticationFilter(jwtService);

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/error").permitAll()
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