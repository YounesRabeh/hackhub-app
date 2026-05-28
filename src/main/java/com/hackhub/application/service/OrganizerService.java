package com.hackhub.application.service;

import com.hackhub.api.dto.request.DeclareWinnerRequest;
import com.hackhub.api.dto.response.HackathonResponse;
import com.hackhub.api.exception.BadRequestException;
import com.hackhub.api.exception.ConflictException;
import com.hackhub.api.exception.ForbiddenException;
import com.hackhub.api.exception.NotFoundException;
import com.hackhub.application.mapper.HackathonMapper;
import com.hackhub.domain.enums.HackathonStatus;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.Submission;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.EvaluationRepository;
import com.hackhub.infrastructure.repository.HackathonRegistrationRepository;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.SubmissionRepository;
import com.hackhub.infrastructure.repository.TeamRepository;
import com.hackhub.infrastructure.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizerService {

	private final HackathonRepository hackathonRepository;
	private final TeamRepository teamRepository;
	private final HackathonRegistrationRepository hackathonRegistrationRepository;
	private final SubmissionRepository submissionRepository;
	private final EvaluationRepository evaluationRepository;
	private final UserRepository userRepository;
	private final StaffAccessService staffAccessService;
	private final HackathonMapper hackathonMapper;

	@Transactional
	public HackathonResponse declareWinner(Long hackathonId, DeclareWinnerRequest request) {
		User currentUser = currentUser();
		Hackathon hackathon = hackathonRepository
			.findById(hackathonId)
			.orElseThrow(() -> new NotFoundException("Hackathon not found"));
		Team winnerTeam = teamRepository
			.findById(request.winnerTeamId())
			.orElseThrow(() -> new NotFoundException("Winner team not found"));

		if (!staffAccessService.isOrganizerOf(currentUser, hackathon)) {
			throw new ForbiddenException("Only organizer can declare winner");
		}
		if (hackathon.getStatus() != HackathonStatus.EVALUATION) {
			throw new BadRequestException("Hackathon must be in EVALUATION");
		}
		if (hackathon.getWinnerTeam() != null) {
			throw new ConflictException("Winner has already been declared");
		}
		if (!hackathonRegistrationRepository.existsByHackathonAndTeam(hackathon, winnerTeam)) {
			throw new BadRequestException("Winner team must be registered");
		}
		if (submissionRepository.findByHackathonAndTeam(hackathon, winnerTeam).isEmpty()) {
			throw new BadRequestException("Winner team must have a submission");
		}

		List<Submission> submissions = submissionRepository.findAllByHackathon(hackathon);
		if (submissions.isEmpty()) {
			throw new BadRequestException("Cannot declare winner without submissions");
		}
		boolean hasUnevaluated = submissions
			.stream()
			.anyMatch(submission -> evaluationRepository.findBySubmission(submission).isEmpty());
		if (hasUnevaluated) {
			throw new BadRequestException("All submissions must be evaluated");
		}

		hackathon.setWinnerTeam(winnerTeam);
		hackathon.setStatus(HackathonStatus.FINISHED);

		return hackathonMapper.toResponse(hackathonRepository.save(hackathon));
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
}
