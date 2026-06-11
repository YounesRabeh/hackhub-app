package com.hackhub.unit.application.service;

import com.hackhub.api.dto.request.CreateTeamRequest;
import com.hackhub.api.exception.ConflictException;
import com.hackhub.application.mapper.TeamMapper;
import com.hackhub.application.service.TeamService;
import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.TeamRepository;
import com.hackhub.infrastructure.repository.UserRepository;
import com.hackhub.testsupport.TestDataFactory;
import com.hackhub.testsupport.TestSecurity;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

	@Mock
	private TeamRepository teamRepository;

	@Mock
	private UserRepository userRepository;

	private TeamService teamService;

	@BeforeEach
	void setUp() {
		teamService = new TeamService(teamRepository, userRepository, new TeamMapper());
	}

	@AfterEach
	void tearDown() {
		TestSecurity.clear();
	}

	@Test
	@SuppressWarnings("all")
	void createTeamAddsCreatorAsFirstMember() {
		User user = TestDataFactory.user(1L, Role.USER);
		TestSecurity.authenticateAs(user);
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(teamRepository.findByMembersContaining(user)).thenReturn(Optional.empty());
		when(teamRepository.save(isA(Team.class))).thenReturn(
			savedTeam(user)
		);

		var response = teamService.createTeam(new CreateTeamRequest(" CodeStorm "));

		verify(teamRepository).save(isA(Team.class));
		assertThat(response.name()).isEqualTo("CodeStorm");
		assertThat(response.memberIds()).containsExactly(1L);
	}

	@Test
	void createTeamRejectsUserAlreadyInTeam() {
		User user = TestDataFactory.user(1L, Role.USER);
		Team existingTeam = TestDataFactory.team(10L, user, user);
		TestSecurity.authenticateAs(user);
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(teamRepository.findByMembersContaining(user)).thenReturn(Optional.of(existingTeam));

		assertThatThrownBy(() -> teamService.createTeam(new CreateTeamRequest("Second Team")))
			.isInstanceOf(ConflictException.class)
			.hasMessage("User already belongs to a team");
	}

	private static Team savedTeam(User user) {
		Team team = TestDataFactory.team(10L, user, user);
		team.setName("CodeStorm");
		return team;
	}
}
