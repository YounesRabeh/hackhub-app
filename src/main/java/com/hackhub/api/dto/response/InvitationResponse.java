package com.hackhub.api.dto.response;

import com.hackhub.domain.enums.InvitationStatus;
import java.time.LocalDateTime;

public record InvitationResponse(
	Long id,
	Long teamId,
	Long invitedUserId,
	Long invitedByUserId,
	InvitationStatus status,
	LocalDateTime createdAt,
	LocalDateTime respondedAt
) {
}
