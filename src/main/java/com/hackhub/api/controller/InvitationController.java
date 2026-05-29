package com.hackhub.api.controller;

import com.hackhub.api.dto.request.CreateInvitationRequest;
import com.hackhub.api.dto.response.InvitationResponse;
import com.hackhub.application.service.InvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
	name = "Invitations",
	description = "Invite users to teams and manage invitation acceptance or decline."
)
public class InvitationController {

	private final InvitationService invitationService;

	@PostMapping("/api/teams/{teamId}/invitations")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
		summary = "Invite user to team",
		description = "Use case: a team member invites another user to join the team."
	)
	public InvitationResponse inviteUser(
		@PathVariable Long teamId,
		@Valid @RequestBody CreateInvitationRequest request
	) {
		return invitationService.inviteUser(teamId, request);
	}

	@PostMapping("/api/invitations/{invitationId}/accept")
	@Operation(
		summary = "Accept invitation",
		description = "Use case: an invited user accepts a pending team invitation."
	)
	public InvitationResponse acceptInvitation(@PathVariable Long invitationId) {
		return invitationService.acceptInvitation(invitationId);
	}

	@PostMapping("/api/invitations/{invitationId}/decline")
	@Operation(
		summary = "Decline invitation",
		description = "Use case: an invited user declines a pending team invitation."
	)
	public InvitationResponse declineInvitation(@PathVariable Long invitationId) {
		return invitationService.declineInvitation(invitationId);
	}
}
