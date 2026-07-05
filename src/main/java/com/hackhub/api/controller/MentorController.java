package com.hackhub.api.controller;

import com.hackhub.api.OpenApiConfig;
import com.hackhub.api.dto.request.CreateSupportRequestRequest;
import com.hackhub.api.dto.request.ProposeCallRequest;
import com.hackhub.api.dto.response.CallProposalResponse;
import com.hackhub.api.dto.response.SupportRequestResponse;
import com.hackhub.application.service.MentorService;
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
	name = "Mentorship",
	description = "Manage support requests and mentor call proposals during hackathons."
)
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class MentorController {

	private final MentorService mentorService;

	@PostMapping("/api/hackathons/{hackathonId}/support-requests")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
		summary = "Create support request",
		description = "Use case: a team asks mentors for help on blockers encountered during a hackathon."
	)
	public SupportRequestResponse createSupportRequest(
		@Parameter(description = "Hackathon ID", example = "1")
		@PathVariable("hackathonId") @NonNull Long hackathonId,
		@Valid @RequestBody @NonNull CreateSupportRequestRequest request
	) {
		return mentorService.createSupportRequest(hackathonId, request);
	}

	@PostMapping("/api/support-requests/{supportRequestId}/call-proposal")
	@Operation(
		summary = "Propose mentor call",
		description = "Use case: a mentor proposes a call slot to handle a support request in real time."
	)
	public CallProposalResponse proposeCall(
		@Parameter(description = "Support request ID", example = "1")
		@PathVariable("supportRequestId") @NonNull Long supportRequestId,
		@Valid @RequestBody @NonNull ProposeCallRequest request
	) {
		return mentorService.proposeCall(supportRequestId, request);
	}
}
