package com.kernith.easyinvoice.data.dto.user;

import com.kernith.easyinvoice.data.model.User;
import com.kernith.easyinvoice.data.model.UserRole;

public record UserSummary(
		Long id,
		String email,
		UserRole role,
		boolean enabled
) {
    public static UserSummary from(User user) {
        return new UserSummary(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled()
        );
    }
}