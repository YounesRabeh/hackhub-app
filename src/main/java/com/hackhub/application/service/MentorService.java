package com.hackhub.application.service;

import com.hackhub.api.dto.request.CreateSupportRequestRequest;
import com.hackhub.api.dto.request.ProposeCallRequest;
import com.hackhub.api.dto.response.CallProposalResponse;
import com.hackhub.api.dto.response.SupportRequestResponse;
import com.hackhub.api.exception.BadRequestException;
import com.hackhub.api.exception.ForbiddenException;
import com.hackhub.api.exception.NotFoundException;
import com.hackhub.domain.enums.HackathonStatus;
import com.hackhub.domain.enums.SupportRequestStatus;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.MentorCallProposal;
import com.hackhub.domain.model.SupportRequest;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.HackathonRegistrationRepository;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.MentorCallProposalRepository;
import com.hackhub.infrastructure.repository.SupportRequestRepository;
import com.hackhub.infrastructure.repository.TeamRepository;
import com.hackhub.infrastructure.repository.UserRepository;
import com.hackhub.infrastructure.external.calendar.CalendarBookingRequest;
import com.hackhub.infrastructure.external.calendar.CalendarBookingResponse;
import com.hackhub.infrastructure.external.calendar.CalendarClient;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentorService {

	private final SupportRequestRepository supportRequestRepository;
	private final HackathonRepository hackathonRepository;
	private final TeamRepository teamRepository;
	private final HackathonRegistrationRepository hackathonRegistrationRepository;
	private final UserRepository userRepository;
	private final StaffAccessService staffAccessService;
	private final MentorCallProposalRepository mentorCallProposalRepository;
	private final CalendarClient calendarClient;

	@Transactional
	public SupportRequestResponse createSupportRequest(
		Long hackathonId,
		CreateSupportRequestRequest request
	) {
		User currentUser = currentUser();
		Hackathon hackathon = loadHackathon(hackathonId);

		if (hackathon.getStatus() != HackathonStatus.IN_PROGRESS) {
			throw new BadRequestException("Hackathon must be IN_PROGRESS");
		}

		Team team = teamRepository
			.findByMembersContaining(currentUser)
			.orElseThrow(() -> new ForbiddenException("Only registered team members can create support requests"));

		if (!hackathonRegistrationRepository.existsByHackathonAndTeam(hackathon, team)) {
			throw new ForbiddenException("Only registered team members can create support requests");
		}

		SupportRequest supportRequest = new SupportRequest();
		supportRequest.setHackathon(hackathon);
		supportRequest.setTeam(team);
		supportRequest.setCreatedByUser(currentUser);
		supportRequest.setTitle(request.title().trim());
		supportRequest.setMessage(request.message().trim());
		supportRequest.setStatus(SupportRequestStatus.OPEN);
		supportRequest.setCreatedAt(LocalDateTime.now());

		return toResponse(supportRequestRepository.save(supportRequest));
	}

	@Transactional(readOnly = true)
	public List<SupportRequestResponse> listSupportRequests(Long hackathonId) {
		User currentUser = currentUser();
		Hackathon hackathon = loadHackathon(hackathonId);

		if (!staffAccessService.isMentorOf(currentUser, hackathon)) {
			throw new ForbiddenException("Only assigned mentors can view support requests");
		}

		return supportRequestRepository
			.findAllByHackathon(hackathon)
			.stream()
			.map(this::toResponse)
			.toList();
	}

	@Transactional
	public CallProposalResponse proposeCall(
		Long supportRequestId,
		ProposeCallRequest request
	) {
		User currentUser = currentUser();
		SupportRequest supportRequest = supportRequestRepository
			.findById(supportRequestId)
			.orElseThrow(() -> new NotFoundException("Support request not found"));

		User assignedMentor = supportRequest.getAssignedMentor();
		if (assignedMentor == null || !assignedMentor.getId().equals(currentUser.getId())) {
			throw new ForbiddenException("Only assigned mentor can propose a call");
		}
		if (supportRequest.getStatus() == SupportRequestStatus.CLOSED) {
			throw new BadRequestException("Cannot propose a call for a closed support request");
		}

		CalendarBookingRequest bookingRequest = new CalendarBookingRequest(
			supportRequest.getTitle(),
			request.scheduledAt(),
			supportRequest.getCreatedByUser().getEmail(),
			currentUser.getEmail()
		);
		CalendarBookingResponse bookingResponse = calendarClient.bookCall(bookingRequest);

		MentorCallProposal proposal = new MentorCallProposal();
		proposal.setSupportRequest(supportRequest);
		proposal.setMentor(currentUser);
		proposal.setScheduledAt(request.scheduledAt());
		proposal.setExternalCallId(bookingResponse.externalCallId());
		proposal.setBookingUrl(bookingResponse.bookingUrl());
		proposal.setCreatedAt(LocalDateTime.now());

		supportRequest.setStatus(SupportRequestStatus.CALL_PROPOSED);
		supportRequestRepository.save(supportRequest);

		return toResponse(mentorCallProposalRepository.save(proposal));
	}

	private Hackathon loadHackathon(Long hackathonId) {
		return hackathonRepository
			.findById(hackathonId)
			.orElseThrow(() -> new NotFoundException("Hackathon not found"));
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

	private SupportRequestResponse toResponse(SupportRequest supportRequest) {
		return new SupportRequestResponse(
			supportRequest.getId(),
			supportRequest.getHackathon().getId(),
			supportRequest.getTeam().getId(),
			supportRequest.getCreatedByUser().getId(),
			supportRequest.getAssignedMentor() == null ? null : supportRequest.getAssignedMentor().getId(),
			supportRequest.getTitle(),
			supportRequest.getMessage(),
			supportRequest.getStatus(),
			supportRequest.getCreatedAt(),
			supportRequest.getClosedAt()
		);
	}

	private CallProposalResponse toResponse(MentorCallProposal proposal) {
		return new CallProposalResponse(
			proposal.getId(),
			proposal.getSupportRequest().getId(),
			proposal.getMentor().getId(),
			proposal.getScheduledAt(),
			proposal.getExternalCallId(),
			proposal.getBookingUrl(),
			proposal.getCreatedAt()
		);
	}
}
