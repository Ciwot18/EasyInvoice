package com.kernith.easyinvoice.data.dto.user;

import com.kernith.easyinvoice.data.model.UserRole;
import java.time.LocalDateTime;

public record ProfileResponse(
		Long id,
		String email,
		UserRole role,
		boolean enabled,
		LocalDateTime createdAt
) {}