package com.hackhub.api.controller;

import com.hackhub.api.dto.request.CreateSupportRequestRequest;
import com.hackhub.api.dto.request.ProposeCallRequest;
import com.hackhub.api.dto.response.CallProposalResponse;
import com.hackhub.api.dto.response.SupportRequestResponse;
import com.hackhub.application.service.MentorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
	name = "Mentorship",
	description = "Manage support requests and mentor call proposals during hackathons."
)
public class MentorController {

	private final MentorService mentorService;

	@PostMapping("/api/hackathons/{hackathonId}/support-requests")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
		summary = "Create support request",
		description = "Use case: a team asks mentors for help on blockers encountered during a hackathon."
	)
	public SupportRequestResponse createSupportRequest(
		@PathVariable Long hackathonId,
		@Valid @RequestBody CreateSupportRequestRequest request
	) {
		return mentorService.createSupportRequest(hackathonId, request);
	}

	@GetMapping("/api/hackathons/{hackathonId}/support-requests")
	@Operation(
		summary = "List support requests",
		description = "Use case: mentors and staff review all support requests for a specific hackathon."
	)
	public List<SupportRequestResponse> listSupportRequests(@PathVariable Long hackathonId) {
		return mentorService.listSupportRequests(hackathonId);
	}

	@PostMapping("/api/support-requests/{supportRequestId}/call-proposal")
	@Operation(
		summary = "Propose mentor call",
		description = "Use case: a mentor proposes a call slot to handle a support request in real time."
	)
	public CallProposalResponse proposeCall(
		@PathVariable Long supportRequestId,
		@Valid @RequestBody ProposeCallRequest request
	) {
		return mentorService.proposeCall(supportRequestId, request);
	}
}
