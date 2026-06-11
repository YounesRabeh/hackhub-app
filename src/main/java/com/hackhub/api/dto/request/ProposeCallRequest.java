package com.hackhub.api.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;

public record ProposeCallRequest(
	@Schema(example = "2099-06-14T16:30:00") @NonNull @NotNull @Future LocalDateTime scheduledAt
) {
}
