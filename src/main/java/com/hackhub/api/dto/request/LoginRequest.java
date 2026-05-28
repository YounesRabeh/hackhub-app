package com.hackhub.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(
	@Schema(example = "user1@example.com") @Email @NotBlank String email,
	@Schema(example = "Password123!") @NotBlank String password
) {
}
