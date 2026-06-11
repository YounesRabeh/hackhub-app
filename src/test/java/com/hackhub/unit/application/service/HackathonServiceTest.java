package com.hackhub.unit.application.service;

import com.hackhub.api.dto.request.CreateHackathonRequest;
import com.hackhub.api.dto.request.UpdateHackathonStatusRequest;
import com.hackhub.api.exception.BadRequestException;
import com.hackhub.api.exception.ForbiddenException;
import com.hackhub.application.mapper.HackathonMapper;
import com.hackhub.application.service.HackathonService;
import com.hackhub.domain.enums.HackathonStatus;
import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.User;
import com.hackhub.domain.state.HackathonStateFactory;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.UserRepository;
import com.hackhub.testsupport.TestDataFactory;
import com.hackhub.testsupport.TestSecurity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HackathonServiceTest {

	@Mock
	private HackathonRepository hackathonRepository;

	@Mock
	private UserRepository userRepository;

	private HackathonService hackathonService;

	@BeforeEach
	void setUp() {
		hackathonService = new HackathonService(
			hackathonRepository,
			userRepository,
			new HackathonMapper(),
			new HackathonStateFactory()
		);
	}

	@AfterEach
	void tearDown() {
		TestSecurity.clear();
	}

	@Test
	void createHackathonRequiresOrganizerRole() {
		User user = TestDataFactory.user(1L, Role.USER);
		TestSecurity.authenticateAs(user);
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> hackathonService.createHackathon(validCreateRequest()))
			.isInstanceOf(ForbiddenException.class)
			.hasMessage("Only organizers can create hackathons");
	}

	@Test
	void createHackathonRejectsInvalidDateOrder() {
		User organizer = TestDataFactory.user(1L, Role.ORGANIZER);
		TestSecurity.authenticateAs(organizer);
		when(userRepository.findByEmail(organizer.getEmail())).thenReturn(Optional.of(organizer));
		LocalDateTime base = LocalDateTime.now().plusDays(3);
		CreateHackathonRequest request = new CreateHackathonRequest(
			"Bad Dates",
			"Registration deadline is after submission deadline",
			base.plusDays(4),
			base.plusDays(2),
			base.plusDays(1),
			base.plusDays(5),
			new BigDecimal("500.00")
		);

		assertThatThrownBy(() -> hackathonService.createHackathon(request))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("Registration deadline must be before submission deadline");
	}

	@Test
	void updateStatusRejectsInvalidTransition() {
		User organizer = TestDataFactory.user(1L, Role.ORGANIZER);
		Hackathon hackathon = TestDataFactory.hackathon(10L, organizer, HackathonStatus.REGISTRATION_OPEN);
		TestSecurity.authenticateAs(organizer);
		when(hackathonRepository.findById(10L)).thenReturn(Optional.of(hackathon));
		when(userRepository.findByEmail(organizer.getEmail())).thenReturn(Optional.of(organizer));

		assertThatThrownBy(() -> hackathonService.updateStatus(
			10L,
			new UpdateHackathonStatusRequest(HackathonStatus.FINISHED)
		))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("Invalid status transition from REGISTRATION_OPEN to FINISHED");
	}

	@Test
	void addMentorRejectsUserWithoutMentorRole() {
		User organizer = TestDataFactory.user(1L, Role.ORGANIZER);
		User notMentor = TestDataFactory.user(2L, Role.USER);
		Hackathon hackathon = TestDataFactory.hackathon(10L, organizer, HackathonStatus.REGISTRATION_OPEN);
		TestSecurity.authenticateAs(organizer);
		when(hackathonRepository.findById(10L)).thenReturn(Optional.of(hackathon));
		when(userRepository.findByEmail(organizer.getEmail())).thenReturn(Optional.of(organizer));
		when(userRepository.findById(2L)).thenReturn(Optional.of(notMentor));

		assertThatThrownBy(() -> hackathonService.addMentor(10L, 2L))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("Assigned user must have role MENTOR");
	}

	@Test
	void addJudgeRejectsUserWithoutJudgeRole() {
		User organizer = TestDataFactory.user(1L, Role.ORGANIZER);
		User notJudge = TestDataFactory.user(2L, Role.USER);
		Hackathon hackathon = TestDataFactory.hackathon(10L, organizer, HackathonStatus.REGISTRATION_OPEN);
		TestSecurity.authenticateAs(organizer);
		when(hackathonRepository.findById(10L)).thenReturn(Optional.of(hackathon));
		when(userRepository.findByEmail(organizer.getEmail())).thenReturn(Optional.of(organizer));
		when(userRepository.findById(2L)).thenReturn(Optional.of(notJudge));

		assertThatThrownBy(() -> hackathonService.addJudge(10L, 2L))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("Assigned user must have role JUDGE");
	}

	@Test
	void createHackathonSetsRegistrationOpenStatus() {
		User organizer = TestDataFactory.user(1L, Role.ORGANIZER);
		TestSecurity.authenticateAs(organizer);
			when(userRepository.findByEmail(organizer.getEmail())).thenReturn(Optional.of(organizer));
			when(hackathonRepository.save(any(Hackathon.class))).thenAnswer(invocation -> {
				Hackathon hackathon = hackathonArgument(invocation);
				hackathon.setId(10L);
				return hackathon;
			});

		var response = hackathonService.createHackathon(validCreateRequest());

		assertThat(response.status()).isEqualTo(HackathonStatus.REGISTRATION_OPEN);
		assertThat(response.organizerId()).isEqualTo(1L);
	}

	@SuppressWarnings("null")
	private @NonNull CreateHackathonRequest validCreateRequest() {
		LocalDateTime base = LocalDateTime.now().plusDays(3);
		return new CreateHackathonRequest(
			"HackHub Test",
			"Build something useful",
			base.plusDays(1),
			base.plusDays(5),
			base.plusDays(2),
			base.plusDays(7),
			new BigDecimal("1000.00")
		);
	}

	@SuppressWarnings("null")
	private static @NonNull Hackathon hackathonArgument(InvocationOnMock invocation) {
		return invocation.getArgument(0);
	}
}
