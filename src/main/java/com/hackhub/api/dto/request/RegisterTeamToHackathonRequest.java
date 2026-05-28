package com.hackhub.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterTeamToHackathonRequest(
	@Schema(example = "2") @NotNull @Positive Long teamId
) {
}
