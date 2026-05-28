package com.hackhub.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupportRequestRequest(
	@NotBlank @Size(max = 255) String title,
	@NotBlank @Size(max = 4000) String message
) {
}
