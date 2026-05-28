package com.hackhub.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEvaluationRequest(
	@NotNull @Min(0) @Max(10) Integer score,
	@NotBlank @Size(max = 4000) String comment
) {
}
