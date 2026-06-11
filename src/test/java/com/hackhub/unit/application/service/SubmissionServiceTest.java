package com.hackhub.unit.application.service;

import com.hackhub.api.dto.request.UpsertSubmissionRequest;
import com.hackhub.api.exception.BadRequestException;
import com.hackhub.api.exception.ForbiddenException;
import com.hackhub.application.mapper.SubmissionMapper;
import com.hackhub.application.service.HackathonService;
import com.hackhub.application.service.StaffAccessService;
import com.hackhub.application.service.SubmissionService;
import com.hackhub.domain.enums.HackathonStatus;
import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.Submission;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.HackathonRegistrationRepository;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.SubmissionRepository;
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
import org.springframework.lang.NonNull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

	@Mock
	private SubmissionRepository submissionRepository;

	@Mock
	private HackathonRepository hackathonRepository;

	@Mock
	private HackathonRegistrationRepository registrationRepository;

	@Mock
	private TeamRepository teamRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private HackathonService hackathonService;

	@Mock
	private StaffAccessService staffAccessService;

	private SubmissionService submissionService;

	@BeforeEach
	void setUp() {
		submissionService = new SubmissionService(
			submissionRepository,
			hackathonRepository,
			registrationRepository,
			teamRepository,
			userRepository,
			new SubmissionMapper(),
			hackathonService,
			staffAccessService
		);
	}

	@AfterEach
	void tearDown() {
		TestSecurity.clear();
	}

	@Test
	void upsertRejectsUnregisteredTeam() {
		User user = TestDataFactory.user(1L, Role.USER);
		Hackathon hackathon = TestDataFactory.hackathon(10L, TestDataFactory.user(2L, Role.ORGANIZER), HackathonStatus.IN_PROGRESS);
		Team team = TestDataFactory.team(20L, user, user);
		TestSecurity.authenticateAs(user);
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(hackathonRepository.findById(10L)).thenReturn(Optional.of(hackathon));
		when(teamRepository.findByMembersContaining(user)).thenReturn(Optional.of(team));
		when(registrationRepository.existsByHackathonAndTeam(hackathon, team)).thenReturn(false);

		assertThatThrownBy(() -> submissionService.upsertMyTeamSubmission(10L, validSubmissionRequest()))
			.isInstanceOf(ForbiddenException.class)
			.hasMessage("Only registered teams can submit");
	}

	@Test
	void upsertRejectsAfterSubmissionDeadline() {
		User user = TestDataFactory.user(1L, Role.USER);
		Hackathon hackathon = TestDataFactory.hackathon(10L, TestDataFactory.user(2L, Role.ORGANIZER), HackathonStatus.IN_PROGRESS);
		hackathon.setSubmissionDeadline(LocalDateTime.now().minusHours(1));
		Team team = TestDataFactory.team(20L, user, user);
		TestSecurity.authenticateAs(user);
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(hackathonRepository.findById(10L)).thenReturn(Optional.of(hackathon));
		when(teamRepository.findByMembersContaining(user)).thenReturn(Optional.of(team));
		when(registrationRepository.existsByHackathonAndTeam(hackathon, team)).thenReturn(true);

		assertThatThrownBy(() -> submissionService.upsertMyTeamSubmission(10L, validSubmissionRequest()))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("Submission deadline has passed");
	}

	@Test
	void upsertCreatesSubmissionForRegisteredTeam() {
		User user = TestDataFactory.user(1L, Role.USER);
		Hackathon hackathon = TestDataFactory.hackathon(10L, TestDataFactory.user(2L, Role.ORGANIZER), HackathonStatus.IN_PROGRESS);
		Team team = TestDataFactory.team(20L, user, user);
		TestSecurity.authenticateAs(user);
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(hackathonRepository.findById(10L)).thenReturn(Optional.of(hackathon));
		when(teamRepository.findByMembersContaining(user)).thenReturn(Optional.of(team));
		when(registrationRepository.existsByHackathonAndTeam(hackathon, team)).thenReturn(true);
		when(submissionRepository.findByHackathonAndTeam(hackathon, team)).thenReturn(Optional.empty());
		when(submissionRepository.save(anySubmission())).thenReturn(
			TestDataFactory.submission(30L, hackathon, team)
		);

		var response = submissionService.upsertMyTeamSubmission(10L, validSubmissionRequest());

		assertThat(response.id()).isEqualTo(30L);
		assertThat(response.teamId()).isEqualTo(20L);
		verify(submissionRepository).save(anySubmission());
	}

	@Test
	void staffListingRejectsUnassignedUser() {
		User user = TestDataFactory.user(1L, Role.JUDGE);
		Hackathon hackathon = TestDataFactory.hackathon(10L, TestDataFactory.user(2L, Role.ORGANIZER), HackathonStatus.EVALUATION);
		TestSecurity.authenticateAs(user);
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(hackathonRepository.findById(10L)).thenReturn(Optional.of(hackathon));
		when(staffAccessService.canAccessSubmissions(user, hackathon)).thenReturn(false);

		assertThatThrownBy(() -> submissionService.listHackathonSubmissions(10L))
			.isInstanceOf(ForbiddenException.class)
			.hasMessage("Only assigned staff can view submissions");
	}

	private @NonNull UpsertSubmissionRequest validSubmissionRequest() {
		return new UpsertSubmissionRequest(
			"AI Study Buddy",
			"https://github.com/example/study-buddy",
			"https://demo.example.com/study-buddy",
			"A tool for study planning"
		);
	}

	@SuppressWarnings("all")
	private static @NonNull Submission anySubmission() {
		return isA(Submission.class);
	}
}
