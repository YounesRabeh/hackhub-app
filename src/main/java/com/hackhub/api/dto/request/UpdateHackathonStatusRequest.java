package com.hackhub.api.dto.request;

import com.hackhub.domain.enums.HackathonStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateHackathonStatusRequest(
	@Schema(example = "IN_PROGRESS") @NotNull HackathonStatus status
) {
}
