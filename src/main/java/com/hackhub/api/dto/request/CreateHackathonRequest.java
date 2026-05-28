package com.hackhub.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateHackathonRequest(
	@NotBlank String title,
	@NotBlank String description,
	@NotNull @Future LocalDateTime registrationDeadline,
	@NotNull @Future LocalDateTime submissionDeadline,
	@NotNull @Future LocalDateTime startAt,
	@NotNull @Future LocalDateTime endAt,
	@NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal prizeAmount
) {
}
