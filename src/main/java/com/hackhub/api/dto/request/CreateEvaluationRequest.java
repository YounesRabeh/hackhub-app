package com.hackhub.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateEvaluationRequest(
	@Schema(example = "8") @NotNull @Min(0) @Max(10) Integer score,
	@Schema(example = "Strong implementation, good presentation, minor UX issues.") @NotBlank @Size(max = 4000) String comment
) {
}
