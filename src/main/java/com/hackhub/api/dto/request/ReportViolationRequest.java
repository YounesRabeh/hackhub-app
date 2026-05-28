package com.hackhub.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record ReportViolationRequest(
	@Schema(example = "3") @NotNull @Positive Long reportedTeamId,
	@Schema(example = "Team submitted copyrighted assets without attribution.") @NotBlank @Size(max = 4000) String description
) {
}
