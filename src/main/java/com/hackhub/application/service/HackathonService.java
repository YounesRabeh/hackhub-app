package com.hackhub.application.service;

import com.hackhub.api.dto.request.CreateHackathonRequest;
import com.hackhub.api.dto.request.UpdateHackathonStatusRequest;
import com.hackhub.api.dto.response.HackathonResponse;
import com.hackhub.api.exception.BadRequestException;
import com.hackhub.api.exception.ForbiddenException;
import com.hackhub.api.exception.NotFoundException;
import com.hackhub.application.mapper.HackathonMapper;
import com.hackhub.domain.enums.HackathonStatus;
import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.User;
import com.hackhub.domain.state.HackathonState;
import com.hackhub.domain.state.HackathonStateFactory;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HackathonService {

	private final HackathonRepository hackathonRepository;
	private final UserRepository userRepository;
	private final HackathonMapper hackathonMapper;
	private final HackathonStateFactory hackathonStateFactory;

	public HackathonService(
		HackathonRepository hackathonRepository,
		UserRepository userRepository,
		HackathonMapper hackathonMapper,
		HackathonStateFactory hackathonStateFactory
	) {
		this.hackathonRepository = hackathonRepository;
		this.userRepository = userRepository;
		this.hackathonMapper = hackathonMapper;
		this.hackathonStateFactory = hackathonStateFactory;
	}

	@Transactional(readOnly = true)
	public List<HackathonResponse> listHackathons() {
		return hackathonRepository.findAll().stream().map(hackathonMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public HackathonResponse getHackathon(Long hackathonId) {
		Hackathon hackathon = loadHackathon(hackathonId);
		return hackathonMapper.toResponse(hackathon);
	}

	@Transactional
	public HackathonResponse createHackathon(CreateHackathonRequest request) {
		User currentUser = currentUser();
		if (currentUser.getRole() != Role.ORGANIZER) {
			throw new ForbiddenException("Only organizers can create hackathons");
		}

		validateDates(
			request.registrationDeadline(),
			request.submissionDeadline(),
			request.startAt(),
			request.endAt()
		);

		Hackathon hackathon = new Hackathon();
		hackathon.setTitle(request.title().trim());
		hackathon.setDescription(request.description().trim());
		hackathon.setRegistrationDeadline(request.registrationDeadline());
		hackathon.setSubmissionDeadline(request.submissionDeadline());
		hackathon.setStartAt(request.startAt());
		hackathon.setEndAt(request.endAt());
		hackathon.setPrizeAmount(request.prizeAmount());
		hackathon.setStatus(HackathonStatus.REGISTRATION_OPEN);
		hackathon.setOrganizer(currentUser);

		Hackathon saved = hackathonRepository.save(hackathon);
		return hackathonMapper.toResponse(saved);
	}

	@Transactional
	public HackathonResponse addMentor(Long hackathonId, Long mentorId) {
		if (mentorId == null) {
			throw new BadRequestException("Mentor id is required");
		}

		Hackathon hackathon = loadHackathon(hackathonId);
		assertWriteAllowed(hackathon);

		User currentUser = currentUser();
		if (!hackathon.getOrganizer().getId().equals(currentUser.getId())) {
			throw new ForbiddenException("Only the organizer can manage mentors");
		}

		User mentor = userRepository
			.findById(mentorId)
			.orElseThrow(() -> new NotFoundException("Mentor not found"));

		if (mentor.getRole() != Role.MENTOR) {
			throw new BadRequestException("Assigned user must have role MENTOR");
		}

		hackathon.getMentors().add(mentor);
		return hackathonMapper.toResponse(hackathon);
	}
	
	@Transactional
	public HackathonResponse updateStatus(
		Long hackathonId,
		UpdateHackathonStatusRequest request
	) {
		Hackathon hackathon = loadHackathon(hackathonId);
		assertWriteAllowed(hackathon);
		User currentUser = currentUser();
		if (!hackathon.getOrganizer().getId().equals(currentUser.getId())) {
			throw new ForbiddenException("Only the organizer can change hackathon status");
		}

		HackathonState state = hackathonStateFactory.fromStatus(hackathon.getStatus());
		HackathonStatus nextStatus = request.status();
		if (!state.canTransitionTo(nextStatus)) {
			throw new BadRequestException(
				"Invalid status transition from " + hackathon.getStatus() + " to " + nextStatus
			);
		}

		hackathon.setStatus(nextStatus);
		return hackathonMapper.toResponse(hackathon);
	}

	public void assertTeamRegistrationAllowed(Hackathon hackathon) {
		HackathonState state = hackathonStateFactory.fromStatus(hackathon.getStatus());
		if (!state.canRegisterTeam()) {
			throw new BadRequestException(
				"Team registration is not allowed in status " + hackathon.getStatus()
			);
		}
	}

	public void assertSubmissionAllowed(Hackathon hackathon) {
		HackathonState state = hackathonStateFactory.fromStatus(hackathon.getStatus());
		if (!state.canSubmit()) {
			throw new BadRequestException(
				"Submission is not allowed in status " + hackathon.getStatus()
			);
		}
	}

	public void assertEvaluationAllowed(Hackathon hackathon) {
		HackathonState state = hackathonStateFactory.fromStatus(hackathon.getStatus());
		if (!state.canEvaluate()) {
			throw new BadRequestException(
				"Evaluation is not allowed in status " + hackathon.getStatus()
			);
		}
	}

	private Hackathon loadHackathon(Long hackathonId) {
		if (hackathonId == null) {
			throw new BadRequestException("Hackathon id is required");
		}

		return hackathonRepository
			.findById(hackathonId)
			.orElseThrow(() -> new NotFoundException("Hackathon not found"));
	}

	private User currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			throw new ForbiddenException("Authentication required");
		}

		return userRepository
			.findByEmail(authentication.getName())
			.orElseThrow(() -> new NotFoundException("User not found"));
	}

	private void validateDates(
		LocalDateTime registrationDeadline,
		LocalDateTime submissionDeadline,
		LocalDateTime startAt,
		LocalDateTime endAt
	) {
		if (!registrationDeadline.isBefore(submissionDeadline)) {
			throw new BadRequestException("Registration deadline must be before submission deadline");
		}
		if (!submissionDeadline.isBefore(endAt)) {
			throw new BadRequestException("Submission deadline must be before end date");
		}
		if (!startAt.isBefore(endAt)) {
			throw new BadRequestException("Start date must be before end date");
		}
	}

	private void assertWriteAllowed(Hackathon hackathon) {
		HackathonState state = hackathonStateFactory.fromStatus(hackathon.getStatus());
		if (!state.allowsWriteOperations()) {
			throw new BadRequestException(
				"Write operations are not allowed when hackathon is " + hackathon.getStatus()
			);
		}
	}
}
