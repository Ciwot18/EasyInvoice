package com.kernith.easyinvoice.data.dto.user;

import com.kernith.easyinvoice.data.model.UserRole;

public record UserSummary(
		Long id,
		String email,
		UserRole role,
		boolean enabled
) {}