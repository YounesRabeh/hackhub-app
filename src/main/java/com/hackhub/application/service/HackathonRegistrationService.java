package com.hackhub.application.service;

import com.hackhub.api.dto.request.RegisterTeamToHackathonRequest;
import com.hackhub.api.dto.response.HackathonRegistrationResponse;
import com.hackhub.api.exception.BadRequestException;
import com.hackhub.api.exception.ConflictException;
import com.hackhub.api.exception.ForbiddenException;
import com.hackhub.api.exception.NotFoundException;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.HackathonRegistration;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.HackathonRegistrationRepository;
import com.hackhub.infrastructure.repository.HackathonRepository;
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
public class HackathonRegistrationService {

	private static final int MAX_TEAM_SIZE = 5;

	private final HackathonRegistrationRepository hackathonRegistrationRepository;
	private final HackathonRepository hackathonRepository;
	private final TeamRepository teamRepository;
	private final UserRepository userRepository;
	private final HackathonService hackathonService;
	private final StaffAccessService staffAccessService;

	//Registers a team for a hackathon after validating eligibility and registration constraints.
	@Transactional
	public HackathonRegistrationResponse registerTeam(
		@NonNull Long hackathonId,
		@NonNull RegisterTeamToHackathonRequest request
	) {
		RegisterTeamToHackathonRequest requiredRequest = requiredRequest(request);
		User currentUser = currentUser();
		Hackathon hackathon = loadHackathon(
			requiredId(hackathonId, "Hackathon ID is required")
		);
		Team team = loadTeam(requiredId(requiredRequest.teamId(), "Team ID is required"));

		if (!teamRepository.existsByIdAndMembersContaining(team.getId(), currentUser)) {
			throw new ForbiddenException("Only team members can register their team");
		}

		hackathonService.assertTeamRegistrationAllowed(hackathon);

		if (LocalDateTime.now().isAfter(hackathon.getRegistrationDeadline())) {
			throw new BadRequestException("Registration deadline has passed");
		}

		if (team.getMembers().size() > MAX_TEAM_SIZE) {
			throw new BadRequestException("Team exceeds maximum allowed size");
		}

		if (hackathonRegistrationRepository.existsByHackathonAndTeam(hackathon, team)) {
			throw new ConflictException("Team is already registered to this hackathon");
		}

		HackathonRegistration registration = new HackathonRegistration();
		registration.setHackathon(hackathon);
		registration.setTeam(team);
		registration.setRegisteredAt(LocalDateTime.now());

		return toResponse(hackathonRegistrationRepository.save(registration));
	}

	//Retrieves all registrations for a hackathon if the current user has permission.
	@Transactional(readOnly = true)
	public List<HackathonRegistrationResponse> listRegistrations(@NonNull Long hackathonId) {
		User currentUser = currentUser();
		Hackathon hackathon = loadHackathon(
			requiredId(hackathonId, "Hackathon ID is required")
		);

		if (!staffAccessService.canAccessSubmissions(currentUser, hackathon)) {
			throw new ForbiddenException("Only assigned staff can view registrations");
		}

		return hackathonRegistrationRepository
			.findAllByHackathon(hackathon)
			.stream()
			.map(this::toResponse)
			.toList();
	}

	//Loads a hackathon by its ID.
	private Hackathon loadHackathon(@NonNull Long hackathonId) {
		return hackathonRepository
			.findById(hackathonId)
			.orElseThrow(() -> new NotFoundException("Hackathon not found"));
	}

	private Team loadTeam(@NonNull Long teamId) {
		return teamRepository.findById(teamId).orElseThrow(() -> new NotFoundException("Team not found"));
	}

	@NonNull
	private Long requiredId(Long id, String message) {
		if (id == null) {
			throw new BadRequestException(message);
		}
		return id;
	}

	@NonNull
	private RegisterTeamToHackathonRequest requiredRequest(
		RegisterTeamToHackathonRequest request
	) {
		if (request == null) {
			throw new BadRequestException("Hackathon registration request is required");
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

	private HackathonRegistrationResponse toResponse(HackathonRegistration registration) {
		return new HackathonRegistrationResponse(
			registration.getId(),
			registration.getHackathon().getId(),
			registration.getTeam().getId(),
			registration.getRegisteredAt()
		);
	}
}
