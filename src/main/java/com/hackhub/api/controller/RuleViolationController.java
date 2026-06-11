package com.hackhub.api.controller;

import com.hackhub.api.OpenApiConfig;
import com.hackhub.api.dto.request.ReportViolationRequest;
import com.hackhub.api.dto.response.RuleViolationReportResponse;
import com.hackhub.application.service.RuleViolationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(
	name = "Rule Violations",
	description = "Report and review potential rule violations during a hackathon."
)
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class RuleViolationController {

	private final RuleViolationService ruleViolationService;

	@PostMapping("/api/hackathons/{hackathonId}/rule-violations")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
		summary = "Report rule violation",
		description = "Use case: a participant or staff member reports suspected rule-breaking behavior."
	)
	public RuleViolationReportResponse createReport(
		@Parameter(description = "Hackathon ID", example = "1")
		@PathVariable("hackathonId") @NonNull Long hackathonId,
		@Valid @RequestBody @NonNull ReportViolationRequest request
	) {
		return ruleViolationService.createReport(hackathonId, request);
	}

	@GetMapping("/api/hackathons/{hackathonId}/rule-violations")
	@Operation(
		summary = "List rule violation reports",
		description = "Use case: organizers or staff review all violation reports for a hackathon."
	)
	public List<RuleViolationReportResponse> listReports(
		@Parameter(description = "Hackathon ID", example = "1")
		@PathVariable("hackathonId") @NonNull Long hackathonId
	) {
		return ruleViolationService.listReports(hackathonId);
	}
}
