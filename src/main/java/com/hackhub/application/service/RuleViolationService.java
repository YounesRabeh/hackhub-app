package com.hackhub.application.service;

import com.hackhub.api.dto.request.ReportViolationRequest;
import com.hackhub.api.dto.response.RuleViolationReportResponse;
import com.hackhub.api.exception.BadRequestException;
import com.hackhub.api.exception.ForbiddenException;
import com.hackhub.api.exception.NotFoundException;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.RuleViolationReport;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.HackathonRegistrationRepository;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.RuleViolationReportRepository;
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
public class RuleViolationService {

	private final RuleViolationReportRepository ruleViolationReportRepository;
	private final HackathonRepository hackathonRepository;
	private final TeamRepository teamRepository;
	private final HackathonRegistrationRepository hackathonRegistrationRepository;
	private final UserRepository userRepository;
	private final StaffAccessService staffAccessService;

	@Transactional
	public RuleViolationReportResponse createReport(
		@NonNull Long hackathonId,
		@NonNull ReportViolationRequest request
	) {
		ReportViolationRequest requiredRequest = requiredRequest(request);
		User currentUser = currentUser();
		Hackathon hackathon = loadHackathon(
			requiredId(hackathonId, "Hackathon ID is required")
		);
		Long reportedTeamId = requiredId(
			requiredRequest.reportedTeamId(),
			"Reported team ID is required"
		);
		Team reportedTeam = teamRepository
			.findById(reportedTeamId)
			.orElseThrow(() -> new NotFoundException("Reported team not found"));

		if (!staffAccessService.isMentorOf(currentUser, hackathon)) {
			throw new ForbiddenException("Only assigned mentors can report violations");
		}

		if (!hackathonRegistrationRepository.existsByHackathonAndTeam(hackathon, reportedTeam)) {
			throw new ForbiddenException("Reported team must be registered to hackathon");
		}

		RuleViolationReport report = new RuleViolationReport();
		report.setHackathon(hackathon);
		report.setReportedTeam(reportedTeam);
		report.setReportedByUser(currentUser);
		report.setDescription(requiredRequest.description().trim());
		report.setCreatedAt(LocalDateTime.now());

		return toResponse(ruleViolationReportRepository.save(report));
	}

	@Transactional(readOnly = true)
	public List<RuleViolationReportResponse> listReports(@NonNull Long hackathonId) {
		User currentUser = currentUser();
		Hackathon hackathon = loadHackathon(
			requiredId(hackathonId, "Hackathon ID is required")
		);

		if (!staffAccessService.isOrganizerOf(currentUser, hackathon)) {
			throw new ForbiddenException("Only organizer can view reports");
		}

		return ruleViolationReportRepository
			.findAllByHackathon(hackathon)
			.stream()
			.map(this::toResponse)
			.toList();
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
	private ReportViolationRequest requiredRequest(ReportViolationRequest request) {
		if (request == null) {
			throw new BadRequestException("Rule violation report is required");
		}
		return request;
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

	private RuleViolationReportResponse toResponse(RuleViolationReport report) {
		return new RuleViolationReportResponse(
			report.getId(),
			report.getHackathon().getId(),
			report.getReportedTeam().getId(),
			report.getReportedByUser().getId(),
			report.getDescription(),
			report.getCreatedAt()
		);
	}
}
