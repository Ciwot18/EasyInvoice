package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.model.User;
import com.kernith.easyinvoice.data.model.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTests {

    @Test
    void generateTokenAndParseClaimsWork() {
        String secret = "0123456789abcdef0123456789abcdef";
        JwtService jwtService = new JwtService(secret, 60);

        Company company = mock(Company.class);
        when(company.getId()).thenReturn(10L);

        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        when(user.getCompany()).thenReturn(company);
        when(user.getRole()).thenReturn(UserRole.COMPANY_MANAGER);

        String token = jwtService.generateToken(user);
        Claims claims = jwtService.parseClaims(token);

        assertNotNull(token);
        assertEquals("7", claims.getSubject());
        assertEquals(10, ((Number) claims.get("cid")).intValue());
        assertEquals("COMPANY_MANAGER", claims.get("role"));
    }
}
