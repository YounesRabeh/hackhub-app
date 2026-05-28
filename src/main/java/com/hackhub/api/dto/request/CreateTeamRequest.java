package com.hackhub.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateTeamRequest(
	@Schema(example = "CodeStorm") @NotBlank @Size(min = 2, max = 80) String name
) {
}
