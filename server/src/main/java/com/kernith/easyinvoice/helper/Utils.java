package com.kernith.easyinvoice.helper;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.model.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public class Utils {
    public static void requireRoles(AuthPrincipal principal, List<UserRole> roles) {
        if (principal == null || principal.role() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing principal");
        }
        UserRole role;
        try {
            role = UserRole.valueOf(principal.role());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid role");
        }
        if (!roles.contains(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role");
        }
    }
    public static String num4(java.math.BigDecimal v) { return v == null ? "" : v.setScale(4).toPlainString(); }
    public static String percent2(java.math.BigDecimal v) { return v == null ? "" : v.setScale(2).toPlainString() + "%"; }
    public static String nvl(String s) { return s == null ? "" : s; }

    public static String money(java.math.BigDecimal v, String currency) {
        if (v == null) v = java.math.BigDecimal.ZERO;
        return symbol(currency) + " " + v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    public static String money4(java.math.BigDecimal v, String currency) {
        if (v == null) v = java.math.BigDecimal.ZERO;
        return symbol(currency) + " " + v.setScale(4, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private static String symbol(String currency) {
        return "EUR".equalsIgnoreCase(currency) ? "€" : currency; // fallback
    }

    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}