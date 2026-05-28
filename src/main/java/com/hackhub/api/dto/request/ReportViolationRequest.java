package com.hackhub.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReportViolationRequest(
	@NotNull @Positive Long reportedTeamId,
	@NotBlank @Size(max = 4000) String description
) {
}
