package com.hackhub.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DeclareWinnerRequest(@NotNull @Positive Long winnerTeamId) {
}
