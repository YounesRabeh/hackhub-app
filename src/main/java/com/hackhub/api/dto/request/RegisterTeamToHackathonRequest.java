package com.hackhub.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;

public record RegisterTeamToHackathonRequest(
	@Schema(example = "2") @NonNull @NotNull @Positive Long teamId
) {
}
