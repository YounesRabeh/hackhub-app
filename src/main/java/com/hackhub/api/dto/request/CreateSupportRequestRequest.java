package com.hackhub.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;

public record CreateSupportRequestRequest(
	@Schema(example = "Need help with deployment") @NonNull @NotBlank @Size(max = 255) String title,
	@Schema(example = "Our backend starts locally but fails on container deployment. Can a mentor help us debug?") @NonNull @NotBlank @Size(max = 4000) String message
) {
}
