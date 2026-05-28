package com.hackhub.api.controller;

import com.hackhub.api.dto.request.ReportViolationRequest;
import com.hackhub.api.dto.response.RuleViolationReportResponse;
import com.hackhub.application.service.RuleViolationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RuleViolationController {

	private final RuleViolationService ruleViolationService;

	@PostMapping("/api/hackathons/{hackathonId}/rule-violations")
	@ResponseStatus(HttpStatus.CREATED)
	public RuleViolationReportResponse createReport(
		@PathVariable Long hackathonId,
		@Valid @RequestBody ReportViolationRequest request
	) {
		return ruleViolationService.createReport(hackathonId, request);
	}

	@GetMapping("/api/hackathons/{hackathonId}/rule-violations")
	public List<RuleViolationReportResponse> listReports(@PathVariable Long hackathonId) {
		return ruleViolationService.listReports(hackathonId);
	}
}
