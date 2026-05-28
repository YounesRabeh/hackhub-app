package com.hackhub.api.dto.request;

import com.hackhub.domain.enums.HackathonStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateHackathonStatusRequest(@NotNull HackathonStatus status) {
}
