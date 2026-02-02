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
}