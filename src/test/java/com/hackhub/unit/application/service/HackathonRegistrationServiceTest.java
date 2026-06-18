package com.hackhub.unit.application.service;

import com.hackhub.api.dto.request.RegisterTeamToHackathonRequest;
import com.hackhub.api.exception.BadRequestException;
import com.hackhub.application.service.HackathonRegistrationService;
import com.hackhub.application.service.HackathonService;
import com.hackhub.application.service.StaffAccessService;
import com.hackhub.domain.enums.HackathonStatus;
import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.HackathonRegistrationRepository;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.TeamRepository;
import com.hackhub.infrastructure.repository.UserRepository;
import com.hackhub.testsupport.TestDataFactory;
import com.hackhub.testsupport.TestSecurity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link HackathonRegistrationService}.
 *
 * <p>The scenarios focus on registration lifecycle rules around whether a
 * current team member may register their team for a hackathon.</p>
 */
@ExtendWith(MockitoExtension.class)
class HackathonRegistrationServiceTest {

	@Mock
	private HackathonRegistrationRepository registrationRepository;

	@Mock
	private HackathonRepository hackathonRepository;

	@Mock
	private TeamRepository teamRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private HackathonService hackathonService;

	@Mock
	private StaffAccessService staffAccessService;

	private HackathonRegistrationService registrationService;

	/**
	 * Creates the service under test with mocked repositories and lifecycle
	 * collaborators.
	 */
	@BeforeEach
	void setUp() {
		registrationService = new HackathonRegistrationService(
			registrationRepository,
			hackathonRepository,
			teamRepository,
			userRepository,
			hackathonService,
			staffAccessService
		);
	}

	/**
	 * Clears the static Spring Security context after each scenario.
	 */
	@AfterEach
	void tearDown() {
		TestSecurity.clear();
	}

	/**
	 * Verifies teams cannot register after the hackathon registration deadline
	 * has passed, even when registration is otherwise open.
	 */
	@Test
	void registerTeamRejectsAfterRegistrationDeadline() {
		User user = TestDataFactory.user(1L, Role.USER);
		Hackathon hackathon = TestDataFactory.hackathon(10L, TestDataFactory.user(2L, Role.ORGANIZER), HackathonStatus.REGISTRATION_OPEN);
		hackathon.setRegistrationDeadline(LocalDateTime.now().minusHours(1));
		Team team = TestDataFactory.team(20L, user, user);
		TestSecurity.authenticateAs(user);
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(hackathonRepository.findById(10L)).thenReturn(Optional.of(hackathon));
		when(teamRepository.findById(20L)).thenReturn(Optional.of(team));
		when(teamRepository.existsByIdAndMembersContaining(20L, user)).thenReturn(true);

		assertThatThrownBy(() -> registrationService.registerTeam(
			10L,
			new RegisterTeamToHackathonRequest(20L)
		))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("Registration deadline has passed");
	}
}
