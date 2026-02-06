package com.kernith.easyinvoice.helper;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.model.DiscountType;
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
    void requireRolesThrowsWhenRoleIsNull() {
        AuthPrincipal principal = new AuthPrincipal(1L, 2L, null, List.of());
        assertThrows(ResponseStatusException.class, () -> Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER)));
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

    @Test
    void escHandlesNullAndSpecialChars() {
        assertEquals("", Utils.esc(null));
        assertEquals("&lt;&gt;&amp;&quot;&#39;", Utils.esc("<>&\"'"));
    }

    @Test
    void getRequiredCompanyIdReturnsCompanyId() {
        AuthPrincipal principal = new AuthPrincipal(1L, 42L, "COMPANY_MANAGER", List.of());
        assertEquals(42L, Utils.getRequiredCompanyId(principal));
    }

    @Test
    void getRequiredCompanyIdThrowsWhenMissingPrincipal() {
        assertThrows(ResponseStatusException.class, () -> Utils.getRequiredCompanyId(null));
    }

    @Test
    void valueNormalizationHelpersWork() {
        assertEquals(new BigDecimal("10.5"), Utils.defaultBigDecimal(new BigDecimal("10.5"), BigDecimal.ONE));
        assertEquals(BigDecimal.ONE, Utils.defaultBigDecimal(null, BigDecimal.ONE));
        assertEquals("test", Utils.normalizeRequired("  test  "));
        assertEquals(null, Utils.normalizeRequired(null));
        assertEquals(null, Utils.trimToNull("   "));
        assertEquals("value", Utils.trimToNull(" value "));
        assertEquals("EUR", Utils.normalizeCurrency(null));
        assertEquals("EUR", Utils.normalizeCurrency(" "));
        assertEquals("USD", Utils.normalizeCurrency(" usd "));
    }

    @Test
    void mapDiscountTypeDefaultsToNone() {
        assertEquals(DiscountType.NONE, Utils.mapDiscountType(null));
        assertEquals(DiscountType.PERCENT, Utils.mapDiscountType(DiscountType.PERCENT));
    }
}
