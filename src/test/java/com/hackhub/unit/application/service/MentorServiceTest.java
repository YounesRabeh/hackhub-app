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
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

	@AfterEach
	void tearDown() {
		TestSecurity.clear();
	}

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
		when(calendarClient.bookCall(any())).thenReturn(new CalendarBookingResponse("external-123", "https://calendar.example/booking"));
		when(callProposalRepository.save(any(MentorCallProposal.class))).thenAnswer(
			MentorServiceTest::savedCallProposal
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

	private @NonNull CreateSupportRequestRequest validSupportRequest() {
		return new CreateSupportRequestRequest("Deployment help", "We need help with deployment.");
	}

	private @NonNull ProposeCallRequest validCallProposalRequest() {
		return new ProposeCallRequest(tomorrow());
	}

	private static @NonNull LocalDateTime tomorrow() {
		return Objects.requireNonNull(LocalDateTime.now()).plusDays(1);
	}

	private static @NonNull MentorCallProposal savedCallProposal(
		InvocationOnMock invocation
	) {
		MentorCallProposal proposal = Objects.requireNonNull(
			invocation.getArgument(0, MentorCallProposal.class)
		);
		proposal.setId(60L);
		return proposal;
	}
}
