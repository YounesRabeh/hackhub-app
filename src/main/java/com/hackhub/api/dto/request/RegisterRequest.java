package com.hackhub.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterRequest(
	@Schema(example = "user1@example.com") @Email @NotBlank String email,
	@Schema(example = "Password123!") @NotBlank @Size(min = 8, max = 100) String password
) {
}
