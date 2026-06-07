package com.hackhub.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateHackathonRequest(
	@Schema(example = "HackHub Spring 2026") @NotBlank String title,
	@Schema(example = "Build an open-source project in 48 hours.") @NotBlank String description,
	@Schema(example = "2099-06-10T12:00:00") @NotNull @Future LocalDateTime registrationDeadline,
	@Schema(example = "2099-06-20T18:00:00") @NotNull @Future LocalDateTime submissionDeadline,
	@Schema(example = "2099-06-12T09:00:00") @NotNull @Future LocalDateTime startAt,
	@Schema(example = "2099-06-22T20:00:00") @NotNull @Future LocalDateTime endAt,
	@Schema(example = "5000.00") @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal prizeAmount
) {
}
