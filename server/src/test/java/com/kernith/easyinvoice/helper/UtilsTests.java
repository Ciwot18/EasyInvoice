package com.kernith.easyinvoice.helper;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.model.UserRole;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UtilsTests {

    @Test
    void requireRolesAllowsMatchingRole() {
        AuthPrincipal principal = new AuthPrincipal(1L, 2L, "COMPANY_MANAGER", List.of());
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER));
    }

    @Test
    void requireRolesThrowsWhenMissingPrincipal() {
        assertThrows(ResponseStatusException.class, () -> Utils.requireRoles(null, List.of(UserRole.COMPANY_MANAGER)));
    }

    @Test
    void requireRolesThrowsWhenInvalidRole() {
        AuthPrincipal principal = new AuthPrincipal(1L, 2L, "NOPE", List.of());
        assertThrows(ResponseStatusException.class, () -> Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER)));
    }

    @Test
    void requireRolesThrowsWhenInsufficientRole() {
        AuthPrincipal principal = new AuthPrincipal(1L, 2L, "BACK_OFFICE", List.of());
        assertThrows(ResponseStatusException.class, () -> Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER)));
    }

    @Test
    void formattingHelpersWork() {
        assertEquals("", Utils.num4(null));
        assertEquals("1.2345", Utils.num4(new BigDecimal("1.2345")));
        assertEquals("", Utils.percent2(null));
        assertEquals("10.50%", Utils.percent2(new BigDecimal("10.5")));
        assertEquals("", Utils.nvl(null));
        assertEquals("test", Utils.nvl("test"));
        assertEquals("€ 0.00", Utils.money(null, "EUR"));
        assertEquals("EURX 2.00", Utils.money(new BigDecimal("2"), "EURX"));
        assertEquals("€ 1.2345", Utils.money4(new BigDecimal("1.2345"), "EUR"));
    }
}
