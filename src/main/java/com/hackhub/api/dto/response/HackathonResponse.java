package com.hackhub.api.dto.response;

import com.hackhub.domain.enums.HackathonStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HackathonResponse(
	Long id,
	String title,
	String description,
	LocalDateTime registrationDeadline,
	LocalDateTime submissionDeadline,
	LocalDateTime startAt,
	LocalDateTime endAt,
	HackathonStatus status,
	BigDecimal prizeAmount,
	Long organizerId,
	Long winnerTeamId
) {
}
