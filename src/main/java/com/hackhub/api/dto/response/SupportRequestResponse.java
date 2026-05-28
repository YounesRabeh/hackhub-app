package com.hackhub.api.dto.response;

import com.hackhub.domain.enums.SupportRequestStatus;
import java.time.LocalDateTime;

public record SupportRequestResponse(
	Long id,
	Long hackathonId,
	Long teamId,
	Long createdByUserId,
	Long assignedMentorId,
	String title,
	String message,
	SupportRequestStatus status,
	LocalDateTime createdAt,
	LocalDateTime closedAt
) {
}
