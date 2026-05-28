package com.hackhub.api.dto.response;

import java.time.LocalDateTime;

public record CallProposalResponse(
	Long id,
	Long supportRequestId,
	Long mentorId,
	LocalDateTime scheduledAt,
	String externalCallId,
	String bookingUrl,
	LocalDateTime createdAt
) {
}
