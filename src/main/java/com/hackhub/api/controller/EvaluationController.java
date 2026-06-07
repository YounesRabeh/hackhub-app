package com.hackhub.api.controller;

import com.hackhub.api.OpenApiConfig;
import com.hackhub.api.dto.request.CreateEvaluationRequest;
import com.hackhub.api.dto.response.EvaluationResponse;
import com.hackhub.application.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(
	name = "Evaluations",
	description = "Score submissions and review evaluation results for a hackathon."
)
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class EvaluationController {

	private final EvaluationService evaluationService;

	@PostMapping("/api/submissions/{submissionId}/evaluation")
	@Operation(
		summary = "Evaluate submission",
		description = "Use case: an assigned judge scores a submission and adds evaluation feedback."
	)
	public EvaluationResponse evaluateSubmission(
		@Parameter(description = "Submission ID", example = "1")
		@PathVariable("submissionId") Long submissionId,
		@Valid @RequestBody CreateEvaluationRequest request
	) {
		return evaluationService.evaluateSubmission(submissionId, request);
	}

	@GetMapping("/api/hackathons/{hackathonId}/evaluations")
	@Operation(
		summary = "List hackathon evaluations",
		description = "Use case: staff review all evaluation records linked to a hackathon."
	)
	public List<EvaluationResponse> listHackathonEvaluations(
		@Parameter(description = "Hackathon ID", example = "1")
		@PathVariable("hackathonId") Long hackathonId
	) {
		return evaluationService.listHackathonEvaluations(hackathonId);
	}
}
