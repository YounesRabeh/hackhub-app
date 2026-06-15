package com.hackhub.unit.application.service;

import com.hackhub.api.dto.request.CreateSupportRequestRequest;
import com.hackhub.api.dto.request.ProposeCallRequest;
import com.hackhub.api.exception.BadRequestException;
import com.hackhub.api.exception.ForbiddenException;
import com.hackhub.application.service.MentorService;
import com.hackhub.application.service.StaffAccessService;
import com.hackhub.domain.enums.HackathonStatus;
import com.hackhub.domain.enums.Role;
import com.hackhub.domain.enums.SupportRequestStatus;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.MentorCallProposal;
import com.hackhub.domain.model.SupportRequest;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.external.calendar.CalendarBookingResponse;
import com.hackhub.infrastructure.external.calendar.CalendarClient;
import com.hackhub.infrastructure.repository.HackathonRegistrationRepository;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.MentorCallProposalRepository;
import com.hackhub.infrastructure.repository.SupportRequestRepository;
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

/**
 * Unit tests for {@link MentorService}.
 *
 * <p>The mocked collaborators let each scenario focus on mentor workflow
 * rules: support requests require an in-progress hackathon and a registered
 * team member, while call proposals must use the calendar client and update
 * support-request state.</p>
 */
@ExtendWith(MockitoExtension.class)
class MentorServiceTest {

	@Mock
	private SupportRequestRepository supportRequestRepository;

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

	@Mock
	private MentorCallProposalRepository callProposalRepository;

	@Mock
	private CalendarClient calendarClient;

	private MentorService mentorService;

	/**
	 * Creates the service under test with mocked repositories, authorization,
	 * and calendar integration.
	 */
	@BeforeEach
	void setUp() {
		mentorService = new MentorService(
			supportRequestRepository,
			hackathonRepository,
			teamRepository,
			registrationRepository,
			userRepository,
			staffAccessService,
			callProposalRepository,
			calendarClient
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
	 * Verifies teams can request mentor help only while the hackathon is in
	 * progress.
	 */
	@Test
	void createSupportRequestRequiresInProgressHackathon() {
		User user = TestDataFactory.user(1L, Role.USER);
		Hackathon hackathon = TestDataFactory.hackathon(10L, TestDataFactory.user(2L, Role.ORGANIZER), HackathonStatus.REGISTRATION_OPEN);
		TestSecurity.authenticateAs(user);
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(hackathonRepository.findById(10L)).thenReturn(Optional.of(hackathon));

		assertThatThrownBy(() -> mentorService.createSupportRequest(10L, validSupportRequest()))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("Hackathon must be IN_PROGRESS");
	}

	/**
	 * Verifies support requests require the authenticated user to belong to a
	 * team registered for the hackathon.
	 */
	@Test
	void createSupportRequestRequiresRegisteredTeamMember() {
		User user = TestDataFactory.user(1L, Role.USER);
		Hackathon hackathon = TestDataFactory.hackathon(10L, TestDataFactory.user(2L, Role.ORGANIZER), HackathonStatus.IN_PROGRESS);
		Team team = TestDataFactory.team(20L, user, user);
		TestSecurity.authenticateAs(user);
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(hackathonRepository.findById(10L)).thenReturn(Optional.of(hackathon));
		when(teamRepository.findByMembersContaining(user)).thenReturn(Optional.of(team));
		when(registrationRepository.existsByHackathonAndTeam(hackathon, team)).thenReturn(false);

		assertThatThrownBy(() -> mentorService.createSupportRequest(10L, validSupportRequest()))
			.isInstanceOf(ForbiddenException.class)
			.hasMessage("Only registered team members can create support requests");
	}

	/**
	 * Verifies proposing a call books through the calendar client, persists the
	 * proposal, and marks the support request as call-proposed.
	 */
	@Test
	void proposeCallUsesCalendarAndMarksSupportRequestCallProposed() {
		User mentor = TestDataFactory.user(1L, Role.MENTOR);
		User requester = TestDataFactory.user(2L, Role.USER);
		Hackathon hackathon = TestDataFactory.hackathon(10L, TestDataFactory.user(3L, Role.ORGANIZER), HackathonStatus.IN_PROGRESS);
		SupportRequest supportRequest = TestDataFactory.supportRequest(
			50L,
			hackathon,
			TestDataFactory.team(20L, requester, requester),
			requester
		);
		supportRequest.setAssignedMentor(mentor);
		TestSecurity.authenticateAs(mentor);
		when(userRepository.findByEmail(mentor.getEmail())).thenReturn(Optional.of(mentor));
		when(supportRequestRepository.findById(50L)).thenReturn(Optional.of(supportRequest));
		when(calendarClient.bookCall(isA(com.hackhub.infrastructure.external.calendar.CalendarBookingRequest.class))).thenReturn(new CalendarBookingResponse("external-123", "https://calendar.example/booking"));
		when(callProposalRepository.save(anyCallProposal())).thenReturn(
			savedCallProposal(supportRequest, mentor)
		);

		var response = mentorService.proposeCall(
			50L,
			validCallProposalRequest()
		);

		assertThat(response.id()).isEqualTo(60L);
		assertThat(response.externalCallId()).isEqualTo("external-123");
		assertThat(response.bookingUrl()).isEqualTo("https://calendar.example/booking");
		assertThat(supportRequest.getStatus()).isEqualTo(SupportRequestStatus.CALL_PROPOSED);
		verify(supportRequestRepository).save(supportRequest);
	}

	/**
	 * Builds a valid support request payload.
	 */
	private @NonNull CreateSupportRequestRequest validSupportRequest() {
		return new CreateSupportRequestRequest("Deployment help", "We need help with deployment.");
	}

	/**
	 * Builds a valid future call proposal payload.
	 */
	private @NonNull ProposeCallRequest validCallProposalRequest() {
		return new ProposeCallRequest(tomorrow());
	}

	/**
	 * Produces a future timestamp that satisfies call proposal validation.
	 */
	private static @NonNull LocalDateTime tomorrow() {
		LocalDateTime now = LocalDateTime.now();
		if (now == null) {
			throw new IllegalStateException("Current time is required");
		}
		LocalDateTime scheduledAt = now.plusDays(1);
		if (scheduledAt == null) {
			throw new IllegalStateException("Scheduled time is required");
		}
		return scheduledAt;
	}

	/**
	 * Builds the saved proposal returned by the mocked repository.
	 */
	private static @NonNull MentorCallProposal savedCallProposal(
		SupportRequest supportRequest,
		User mentor
	) {
		MentorCallProposal proposal = new MentorCallProposal();
		proposal.setId(60L);
		proposal.setSupportRequest(supportRequest);
		proposal.setMentor(mentor);
		proposal.setScheduledAt(tomorrow());
		proposal.setExternalCallId("external-123");
		proposal.setBookingUrl("https://calendar.example/booking");
		proposal.setCreatedAt(tomorrow());
		return proposal;
	}

	/**
	 * Mockito matcher for repository saves that accept any
	 * {@link MentorCallProposal}.
	 */
	@SuppressWarnings("all")
	private static @NonNull MentorCallProposal anyCallProposal() {
		return isA(MentorCallProposal.class);
	}
}
