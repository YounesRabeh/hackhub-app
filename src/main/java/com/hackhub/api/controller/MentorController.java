package com.hackhub.api.controller;

import com.hackhub.api.dto.request.CreateSupportRequestRequest;
import com.hackhub.api.dto.request.ProposeCallRequest;
import com.hackhub.api.dto.response.CallProposalResponse;
import com.hackhub.api.dto.response.SupportRequestResponse;
import com.hackhub.application.service.MentorService;
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
public class MentorController {

	private final MentorService mentorService;

	@PostMapping("/api/hackathons/{hackathonId}/support-requests")
	@ResponseStatus(HttpStatus.CREATED)
	public SupportRequestResponse createSupportRequest(
		@PathVariable Long hackathonId,
		@Valid @RequestBody CreateSupportRequestRequest request
	) {
		return mentorService.createSupportRequest(hackathonId, request);
	}

	@GetMapping("/api/hackathons/{hackathonId}/support-requests")
	public List<SupportRequestResponse> listSupportRequests(@PathVariable Long hackathonId) {
		return mentorService.listSupportRequests(hackathonId);
	}

	@PostMapping("/api/support-requests/{supportRequestId}/call-proposal")
	public CallProposalResponse proposeCall(
		@PathVariable Long supportRequestId,
		@Valid @RequestBody ProposeCallRequest request
	) {
		return mentorService.proposeCall(supportRequestId, request);
	}
}
