package com.hackhub.unit.application.service;

import com.hackhub.api.dto.request.ReportViolationRequest;
import com.hackhub.api.exception.ForbiddenException;
import com.hackhub.application.service.RuleViolationService;
import com.hackhub.application.service.StaffAccessService;
import com.hackhub.domain.enums.HackathonStatus;
import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.RuleViolationReport;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.HackathonRegistrationRepository;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.RuleViolationReportRepository;
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
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RuleViolationService}.
 *
 * <p>The scenarios focus on staff-only access: only assigned mentors can
 * create violation reports, and only the organizer can list reports for a
 * hackathon.</p>
 */
@ExtendWith(MockitoExtension.class)
class RuleViolationServiceTest {

	@Mock
	private RuleViolationReportRepository ruleViolationReportRepository;

	@Mock
	private HackathonRepository hackathonRepository;

	@Mock
	private TeamRepository teamRepository;

	@Mock
	private HackathonRegistrationRepository registrationRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private StaffAccessService staffAccessService;

	private RuleViolationService ruleViolationService;

	/**
	 * Creates the service under test with mocked persistence and authorization
	 * collaborators.
	 */
	@BeforeEach
	void setUp() {
		ruleViolationService = new RuleViolationService(
			ruleViolationReportRepository,
			hackathonRepository,
			teamRepository,
			registrationRepository,
			userRepository,
			staffAccessService
		);
	}

	/**
	 * Clears the Spring Security context after each scenario.
	 */
	@AfterEach
	void tearDown() {
		TestSecurity.clear();
	}

	/**
	 * Verifies mentors cannot report violations for hackathons where they are
	 * not assigned.
	 */
	@Test
	void createReportRejectsUnassignedMentor() {
		User mentor = TestDataFactory.user(1L, Role.MENTOR);
		Hackathon hackathon = TestDataFactory.hackathon(10L, TestDataFactory.user(2L, Role.ORGANIZER), HackathonStatus.IN_PROGRESS);
		Team team = TestDataFactory.team(20L, TestDataFactory.user(3L, Role.USER), TestDataFactory.user(3L, Role.USER));
		TestSecurity.authenticateAs(mentor);
		when(userRepository.findByEmail(mentor.getEmail())).thenReturn(Optional.of(mentor));
		when(hackathonRepository.findById(10L)).thenReturn(Optional.of(hackathon));
		when(teamRepository.findById(20L)).thenReturn(Optional.of(team));
		when(staffAccessService.isMentorOf(mentor, hackathon)).thenReturn(false);

		assertThatThrownBy(() -> ruleViolationService.createReport(10L, validReportRequest()))
			.isInstanceOf(ForbiddenException.class)
			.hasMessage("Only assigned mentors can report violations");
	}

	/**
	 * Verifies ordinary users cannot list staff-only violation reports.
	 */
	@Test
	void listReportsRejectsParticipantUser() {
		User user = TestDataFactory.user(1L, Role.USER);
		Hackathon hackathon = TestDataFactory.hackathon(10L, TestDataFactory.user(2L, Role.ORGANIZER), HackathonStatus.IN_PROGRESS);
		TestSecurity.authenticateAs(user);
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(hackathonRepository.findById(10L)).thenReturn(Optional.of(hackathon));
		when(staffAccessService.isOrganizerOf(user, hackathon)).thenReturn(false);

		assertThatThrownBy(() -> ruleViolationService.listReports(10L))
			.isInstanceOf(ForbiddenException.class)
			.hasMessage("Only organizer can view reports");
	}

	/**
	 * Verifies organizers can list violation reports for their own hackathon.
	 */
	@Test
	void listReportsAllowsOrganizer() {
		User organizer = TestDataFactory.user(1L, Role.ORGANIZER);
		Hackathon hackathon = TestDataFactory.hackathon(10L, organizer, HackathonStatus.IN_PROGRESS);
		Team team = TestDataFactory.team(20L, TestDataFactory.user(2L, Role.USER), TestDataFactory.user(2L, Role.USER));
		RuleViolationReport report = report(40L, hackathon, team, organizer);
		TestSecurity.authenticateAs(organizer);
		when(userRepository.findByEmail(organizer.getEmail())).thenReturn(Optional.of(organizer));
		when(hackathonRepository.findById(10L)).thenReturn(Optional.of(hackathon));
		when(staffAccessService.isOrganizerOf(organizer, hackathon)).thenReturn(true);
		when(ruleViolationReportRepository.findAllByHackathon(hackathon)).thenReturn(java.util.List.of(report));

		var response = ruleViolationService.listReports(10L);

		assertThat(response).hasSize(1);
		assertThat(response.get(0).id()).isEqualTo(40L);
	}

	/**
	 * Verifies assigned mentors can create reports for registered teams.
	 */
	@Test
	void createReportAllowsAssignedMentorForRegisteredTeam() {
		User mentor = TestDataFactory.user(1L, Role.MENTOR);
		Hackathon hackathon = TestDataFactory.hackathon(10L, TestDataFactory.user(2L, Role.ORGANIZER), HackathonStatus.IN_PROGRESS);
		Team team = TestDataFactory.team(20L, TestDataFactory.user(3L, Role.USER), TestDataFactory.user(3L, Role.USER));
		RuleViolationReport report = report(40L, hackathon, team, mentor);
		TestSecurity.authenticateAs(mentor);
		when(userRepository.findByEmail(mentor.getEmail())).thenReturn(Optional.of(mentor));
		when(hackathonRepository.findById(10L)).thenReturn(Optional.of(hackathon));
		when(teamRepository.findById(20L)).thenReturn(Optional.of(team));
		when(staffAccessService.isMentorOf(mentor, hackathon)).thenReturn(true);
		when(registrationRepository.existsByHackathonAndTeam(hackathon, team)).thenReturn(true);
		when(ruleViolationReportRepository.save(anyReport())).thenReturn(report);

		var response = ruleViolationService.createReport(10L, validReportRequest());

		assertThat(response.id()).isEqualTo(40L);
		assertThat(response.reportedByUserId()).isEqualTo(1L);
	}

	/**
	 * Builds a valid report request for team {@code 20}.
	 */
	private @NonNull ReportViolationRequest validReportRequest() {
		return new ReportViolationRequest(20L, "Suspicious submission activity");
	}

	/**
	 * Builds a persisted report fixture returned by mocked repositories.
	 */
	private static @NonNull RuleViolationReport report(
		Long id,
		Hackathon hackathon,
		Team team,
		User reportedBy
	) {
		RuleViolationReport report = new RuleViolationReport();
		report.setId(id);
		report.setHackathon(hackathon);
		report.setReportedTeam(team);
		report.setReportedByUser(reportedBy);
		report.setDescription("Suspicious submission activity");
		report.setCreatedAt(LocalDateTime.now());
		return report;
	}

	/**
	 * Mockito matcher for repository saves that accept any report.
	 */
	@SuppressWarnings("all")
	private static @NonNull RuleViolationReport anyReport() {
		return isA(RuleViolationReport.class);
	}
}
