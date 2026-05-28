package com.hackhub.application.service;

import com.hackhub.api.dto.request.CreateTeamRequest;
import com.hackhub.api.dto.response.TeamResponse;
import com.hackhub.api.exception.ConflictException;
import com.hackhub.api.exception.NotFoundException;
import com.hackhub.application.mapper.TeamMapper;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.TeamRepository;
import com.hackhub.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {

	private final TeamRepository teamRepository;
	private final UserRepository userRepository;
	private final TeamMapper teamMapper;

	@Transactional
	public TeamResponse createTeam(CreateTeamRequest request) {
		User currentUser = currentUser();
		assertUserWithoutTeam(currentUser);

		Team team = new Team();
		team.setName(request.name().trim());
		team.setCreatedBy(currentUser);
		team.getMembers().add(currentUser);

		Team saved = teamRepository.save(team);
		return teamMapper.toResponse(saved);
	}

	@Transactional(readOnly = true)
	public TeamResponse currentUserTeam() {
		User currentUser = currentUser();
		Team team = teamRepository
			.findByMembersContaining(currentUser)
			.orElseThrow(() -> new NotFoundException("Current user is not in any team"));
		return teamMapper.toResponse(team);
	}

	private void assertUserWithoutTeam(User user) {
		if (teamRepository.findByMembersContaining(user).isPresent()) {
			throw new ConflictException("User already belongs to a team");
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
}
