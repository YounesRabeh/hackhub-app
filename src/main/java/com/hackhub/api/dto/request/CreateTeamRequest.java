package com.hackhub.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTeamRequest(@NotBlank @Size(min = 2, max = 80) String name) {
}
