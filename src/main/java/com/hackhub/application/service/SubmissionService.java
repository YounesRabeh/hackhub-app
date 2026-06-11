package com.hackhub.application.service;

import com.hackhub.api.dto.request.UpsertSubmissionRequest;
import com.hackhub.api.dto.response.SubmissionResponse;
import com.hackhub.api.exception.BadRequestException;
import com.hackhub.api.exception.ForbiddenException;
import com.hackhub.api.exception.NotFoundException;
import com.hackhub.application.mapper.SubmissionMapper;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.Submission;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.HackathonRegistrationRepository;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.SubmissionRepository;
import com.hackhub.infrastructure.repository.TeamRepository;
import com.hackhub.infrastructure.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmissionService {

	private final SubmissionRepository submissionRepository;
	private final HackathonRepository hackathonRepository;
	private final HackathonRegistrationRepository hackathonRegistrationRepository;
	private final TeamRepository teamRepository;
	private final UserRepository userRepository;
	private final SubmissionMapper submissionMapper;
	private final HackathonService hackathonService;
	private final StaffAccessService staffAccessService;

	@Transactional
	public SubmissionResponse upsertMyTeamSubmission(
		@NonNull Long hackathonId,
		@NonNull UpsertSubmissionRequest request
	) {
		UpsertSubmissionRequest requiredRequest = requiredRequest(request);
		User currentUser = currentUser();
		Hackathon hackathon = loadHackathon(
			requiredId(hackathonId, "Hackathon ID is required")
		);
		Team team = teamRepository
			.findByMembersContaining(currentUser)
			.orElseThrow(() -> new NotFoundException("Current user is not in any team"));

		hackathonService.assertSubmissionAllowed(hackathon);

		if (!hackathonRegistrationRepository.existsByHackathonAndTeam(hackathon, team)) {
			throw new ForbiddenException("Only registered teams can submit");
		}

		if (LocalDateTime.now().isAfter(hackathon.getSubmissionDeadline())) {
			throw new BadRequestException("Submission deadline has passed");
		}

		Submission submission = submissionRepository
			.findByHackathonAndTeam(hackathon, team)
			.orElseGet(Submission::new);
		boolean isNew = submission.getId() == null;

		submission.setHackathon(hackathon);
		submission.setTeam(team);
		submission.setProjectName(requiredRequest.projectName().trim());
		submission.setRepositoryUrl(requiredRequest.repositoryUrl().trim());
		submission.setDemoUrl(
			requiredRequest.demoUrl() == null ? null : requiredRequest.demoUrl().trim()
		);
		submission.setDescription(requiredRequest.description().trim());
		if (isNew) {
			submission.setSubmittedAt(LocalDateTime.now());
		}
		submission.setUpdatedAt(LocalDateTime.now());

		return submissionMapper.toResponse(submissionRepository.save(submission));
	}

	@Transactional(readOnly = true)
	public SubmissionResponse getMyTeamSubmission(@NonNull Long hackathonId) {
		User currentUser = currentUser();
		Hackathon hackathon = loadHackathon(
			requiredId(hackathonId, "Hackathon ID is required")
		);
		Team team = teamRepository
			.findByMembersContaining(currentUser)
			.orElseThrow(() -> new NotFoundException("Current user is not in any team"));

		if (!hackathonRegistrationRepository.existsByHackathonAndTeam(hackathon, team)) {
			throw new ForbiddenException("Only registered teams can view this submission");
		}

		Submission submission = submissionRepository
			.findByHackathonAndTeam(hackathon, team)
			.orElseThrow(() -> new NotFoundException("Submission not found"));
		return submissionMapper.toResponse(submission);
	}

	@Transactional(readOnly = true)
	public List<SubmissionResponse> listHackathonSubmissions(@NonNull Long hackathonId) {
		User currentUser = currentUser();
		Hackathon hackathon = loadHackathon(
			requiredId(hackathonId, "Hackathon ID is required")
		);

		if (!staffAccessService.canAccessSubmissions(currentUser, hackathon)) {
			throw new ForbiddenException("Only assigned staff can view submissions");
		}

		return submissionRepository
			.findAllByHackathon(hackathon)
			.stream()
			.map(submissionMapper::toResponse)
			.toList();
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

	private Hackathon loadHackathon(@NonNull Long hackathonId) {
		return hackathonRepository
			.findById(hackathonId)
			.orElseThrow(() -> new NotFoundException("Hackathon not found"));
	}

	@NonNull
	private Long requiredId(Long id, String message) {
		if (id == null) {
			throw new BadRequestException(message);
		}
		return id;
	}

	@NonNull
	private UpsertSubmissionRequest requiredRequest(UpsertSubmissionRequest request) {
		if (request == null) {
			throw new BadRequestException("Submission request is required");
		}
		return request;
	}
}
