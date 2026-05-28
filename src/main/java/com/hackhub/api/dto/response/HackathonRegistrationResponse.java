package com.hackhub.api.dto.response;

import java.time.LocalDateTime;

public record HackathonRegistrationResponse(
	Long id,
	Long hackathonId,
	Long teamId,
	LocalDateTime registeredAt
) {
}
