package com.hackhub.api.controller;

import com.hackhub.api.dto.request.CreateInvitationRequest;
import com.hackhub.api.dto.response.InvitationResponse;
import com.hackhub.application.service.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InvitationController {

	private final InvitationService invitationService;

	@PostMapping("/api/teams/{teamId}/invitations")
	@ResponseStatus(HttpStatus.CREATED)
	public InvitationResponse inviteUser(
		@PathVariable Long teamId,
		@Valid @RequestBody CreateInvitationRequest request
	) {
		return invitationService.inviteUser(teamId, request);
	}

	@PostMapping("/api/invitations/{invitationId}/accept")
	public InvitationResponse acceptInvitation(@PathVariable Long invitationId) {
		return invitationService.acceptInvitation(invitationId);
	}

	@PostMapping("/api/invitations/{invitationId}/decline")
	public InvitationResponse declineInvitation(@PathVariable Long invitationId) {
		return invitationService.declineInvitation(invitationId);
	}
}
