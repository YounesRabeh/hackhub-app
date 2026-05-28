package com.hackhub.api.dto.response;

import java.time.LocalDateTime;

public record SubmissionResponse(
	Long id,
	Long hackathonId,
	Long teamId,
	String projectName,
	String repositoryUrl,
	String demoUrl,
	String description,
	LocalDateTime submittedAt,
	LocalDateTime updatedAt
) {
}
