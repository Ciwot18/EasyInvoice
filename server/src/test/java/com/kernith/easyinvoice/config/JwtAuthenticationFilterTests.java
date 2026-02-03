package com.kernith.easyinvoice.config;

import com.kernith.easyinvoice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTests {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotFilterSkipsAuthAndErrorPaths() {
        JwtService jwtService = mock(JwtService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

        MockHttpServletRequest authRequest = new MockHttpServletRequest();
        authRequest.setRequestURI("/auth/login");
        assertTrue(filter.shouldNotFilter(authRequest));

        MockHttpServletRequest errorRequest = new MockHttpServletRequest();
        errorRequest.setRequestURI("/error");
        assertTrue(filter.shouldNotFilter(errorRequest));

        MockHttpServletRequest apiRequest = new MockHttpServletRequest();
        apiRequest.setRequestURI("/api/platform/companies");
        assertTrue(!filter.shouldNotFilter(apiRequest));
    }

    @Test
    void doFilterInternalPassesThroughWhenNoAuthorizationHeader() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/platform/companies");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternalClearsContextWhenTokenInvalid() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        when(jwtService.parseClaims(anyString())).thenThrow(new RuntimeException("bad token"));
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/platform/companies");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer badtoken");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternalSetsAuthenticationWhenTokenValid() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        Claims claims = new DefaultClaims();
        claims.setSubject("7");
        claims.put("cid", 10L);
        claims.put("role", "COMPANY_MANAGER");
        when(jwtService.parseClaims("goodtoken")).thenReturn(claims);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/platform/companies");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer goodtoken");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        AuthPrincipal principal = (AuthPrincipal) auth.getPrincipal();
        assertEquals(7L, principal.userId());
        assertEquals(10L, principal.companyId());
        assertEquals("COMPANY_MANAGER", principal.role());
        verify(chain).doFilter(request, response);
    }
}
