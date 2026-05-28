package com.hackhub.application.service;

import com.hackhub.api.dto.request.CreateInvitationRequest;
import com.hackhub.api.dto.response.InvitationResponse;
import com.hackhub.api.exception.BadRequestException;
import com.hackhub.api.exception.ConflictException;
import com.hackhub.api.exception.ForbiddenException;
import com.hackhub.api.exception.NotFoundException;
import com.hackhub.domain.enums.InvitationStatus;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.TeamInvitation;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.TeamInvitationRepository;
import com.hackhub.infrastructure.repository.TeamRepository;
import com.hackhub.infrastructure.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvitationService {

	private final TeamInvitationRepository teamInvitationRepository;
	private final TeamRepository teamRepository;
	private final UserRepository userRepository;

	@Transactional
	public InvitationResponse inviteUser(Long teamId, CreateInvitationRequest request) {
		User currentUser = currentUser();
		Team team = teamRepository
			.findById(teamId)
			.orElseThrow(() -> new NotFoundException("Team not found"));

		if (!teamRepository.existsByIdAndMembersContaining(teamId, currentUser)) {
			throw new ForbiddenException("Only team members can invite users");
		}

		User invitedUser = userRepository
			.findById(request.invitedUserId())
			.orElseThrow(() -> new NotFoundException("Invited user not found"));

		if (teamRepository.findByMembersContaining(invitedUser).isPresent()) {
			throw new ConflictException("Invited user already belongs to a team");
		}

		if (teamInvitationRepository.findByTeamAndInvitedUser(team, invitedUser).isPresent()) {
			throw new ConflictException("Invitation already exists for this user and team");
		}

		TeamInvitation invitation = new TeamInvitation();
		invitation.setTeam(team);
		invitation.setInvitedUser(invitedUser);
		invitation.setInvitedByUser(currentUser);
		invitation.setStatus(InvitationStatus.PENDING);
		invitation.setCreatedAt(LocalDateTime.now());

		return toResponse(teamInvitationRepository.save(invitation));
	}

	@Transactional
	public InvitationResponse acceptInvitation(Long invitationId) {
		User currentUser = currentUser();
		TeamInvitation invitation = loadInvitation(invitationId);
		assertInvitedUser(invitation, currentUser);
		assertPending(invitation);

		if (teamRepository.findByMembersContaining(currentUser).isPresent()) {
			throw new ConflictException("User already belongs to a team");
		}

		invitation.getTeam().getMembers().add(currentUser);
		invitation.setStatus(InvitationStatus.ACCEPTED);
		invitation.setRespondedAt(LocalDateTime.now());

		return toResponse(invitation);
	}

	@Transactional
	public InvitationResponse declineInvitation(Long invitationId) {
		User currentUser = currentUser();
		TeamInvitation invitation = loadInvitation(invitationId);
		assertInvitedUser(invitation, currentUser);
		assertPending(invitation);

		invitation.setStatus(InvitationStatus.DECLINED);
		invitation.setRespondedAt(LocalDateTime.now());

		return toResponse(invitation);
	}

	private TeamInvitation loadInvitation(Long invitationId) {
		return teamInvitationRepository
			.findById(invitationId)
			.orElseThrow(() -> new NotFoundException("Invitation not found"));
	}

	private void assertInvitedUser(TeamInvitation invitation, User currentUser) {
		if (!invitation.getInvitedUser().getId().equals(currentUser.getId())) {
			throw new ForbiddenException("Only invited user can respond to this invitation");
		}
	}

	private void assertPending(TeamInvitation invitation) {
		if (invitation.getStatus() != InvitationStatus.PENDING) {
			throw new BadRequestException("Invitation has already been answered");
		}
	}

	private User currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			throw new NotFoundException("Authenticated user not found");
		}

		return userRepository
			.findByEmail(authentication.getName())
			.orElseThrow(() -> new NotFoundException("Authenticated user not found"));
	}

	private InvitationResponse toResponse(TeamInvitation invitation) {
		return new InvitationResponse(
			invitation.getId(),
			invitation.getTeam().getId(),
			invitation.getInvitedUser().getId(),
			invitation.getInvitedByUser().getId(),
			invitation.getStatus(),
			invitation.getCreatedAt(),
			invitation.getRespondedAt()
		);
	}
}
