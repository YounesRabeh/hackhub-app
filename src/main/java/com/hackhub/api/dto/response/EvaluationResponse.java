package com.hackhub.api.dto.response;

import java.time.LocalDateTime;

public record EvaluationResponse(
	Long id,
	Long submissionId,
	Long judgeId,
	Integer score,
	String comment,
	LocalDateTime evaluatedAt
) {
}
