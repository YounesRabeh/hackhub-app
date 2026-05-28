package com.hackhub.api.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ProposeCallRequest(@NotNull @Future LocalDateTime scheduledAt) {
}
