package com.kernith.easyinvoice.helper;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.model.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Small utility helpers used across the application.
 */
public class Utils {
    /**
     * Ensures the authenticated principal has one of the allowed roles.
     *
     * @param principal authenticated principal
     * @param roles allowed roles
     * @throws ResponseStatusException if the principal is missing, invalid, or lacks permissions
     */
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
    /**
     * Formats a numeric value with 4 decimal places or returns an empty string if null.
     *
     * @param v numeric value
     * @return formatted value or empty string
     */
    public static String num4(java.math.BigDecimal v) { return v == null ? "" : v.setScale(4).toPlainString(); }
    /**
     * Formats a percent with 2 decimal places or returns an empty string if null.
     *
     * @param v percent value
     * @return formatted percent or empty string
     */
    public static String percent2(java.math.BigDecimal v) { return v == null ? "" : v.setScale(2).toPlainString() + "%"; }
    /**
     * Returns an empty string for null values.
     *
     * @param s input string
     * @return input or empty string if null
     */
    public static String nvl(String s) { return s == null ? "" : s; }

    /**
     * Formats a monetary amount with 2 decimal places and a currency symbol prefix.
     *
     * @param v monetary value
     * @param currency currency code
     * @return formatted monetary value
     */
    public static String money(java.math.BigDecimal v, String currency) {
        if (v == null) v = java.math.BigDecimal.ZERO;
        return symbol(currency) + " " + v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Formats a monetary amount with 4 decimal places and a currency symbol prefix.
     *
     * @param v monetary value
     * @param currency currency code
     * @return formatted monetary value
     */
    public static String money4(java.math.BigDecimal v, String currency) {
        if (v == null) v = java.math.BigDecimal.ZERO;
        return symbol(currency) + " " + v.setScale(4, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Returns the display symbol for a currency code.
     *
     * @param currency currency code
     * @return currency symbol or code fallback
     */
    private static String symbol(String currency) {
        return "EUR".equalsIgnoreCase(currency) ? "€" : currency; // fallback
    }

    /**
     * Escapes a string for safe HTML rendering.
     *
     * @param s input string
     * @return escaped HTML string
     */
    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
