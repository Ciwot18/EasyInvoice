package com.kernith.easyinvoice.data.dto.user;

import com.kernith.easyinvoice.data.model.User;
import com.kernith.easyinvoice.data.model.UserRole;
import java.time.LocalDateTime;

public record ProfileResponse(
		Long id,
		String email,
		String name,
		UserRole role,
		boolean enabled,
		LocalDateTime createdAt
) {
    public static ProfileResponse from(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
