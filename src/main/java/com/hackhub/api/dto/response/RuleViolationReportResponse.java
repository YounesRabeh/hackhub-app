package com.hackhub.api.dto.response;

import java.time.LocalDateTime;

public record RuleViolationReportResponse(
	Long id,
	Long hackathonId,
	Long reportedTeamId,
	Long reportedByUserId,
	String description,
	LocalDateTime createdAt
) {
}
