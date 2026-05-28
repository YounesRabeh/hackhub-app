package com.hackhub.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateSupportRequestRequest(
	@Schema(example = "Need help with deployment") @NotBlank @Size(max = 255) String title,
	@Schema(example = "Our backend starts locally but fails on container deployment. Can a mentor help us debug?") @NotBlank @Size(max = 4000) String message
) {
}
