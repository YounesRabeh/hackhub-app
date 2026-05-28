package com.hackhub.api.controller;

import com.hackhub.api.dto.request.CreateEvaluationRequest;
import com.hackhub.api.dto.response.EvaluationResponse;
import com.hackhub.application.service.EvaluationService;
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
public class EvaluationController {

	private final EvaluationService evaluationService;

	@PostMapping("/api/submissions/{submissionId}/evaluation")
	public EvaluationResponse evaluateSubmission(
		@PathVariable Long submissionId,
		@Valid @RequestBody CreateEvaluationRequest request
	) {
		return evaluationService.evaluateSubmission(submissionId, request);
	}

	@GetMapping("/api/hackathons/{hackathonId}/evaluations")
	public List<EvaluationResponse> listHackathonEvaluations(@PathVariable Long hackathonId) {
		return evaluationService.listHackathonEvaluations(hackathonId);
	}
}
