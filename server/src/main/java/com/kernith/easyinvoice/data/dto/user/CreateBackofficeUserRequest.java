package com.kernith.easyinvoice.data.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBackofficeUserRequest(
		@NotBlank @Email String email,
		@NotBlank @Size(max = 80) String name,
		@NotBlank @Size(min = 8, max = 72) String password
) {}
