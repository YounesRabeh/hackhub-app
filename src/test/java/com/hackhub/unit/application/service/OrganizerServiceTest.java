package com.hackhub.unit.application.service;

import com.hackhub.api.dto.request.DeclareWinnerRequest;
import com.hackhub.api.exception.BadRequestException;
import com.hackhub.application.mapper.HackathonMapper;
import com.hackhub.application.service.OrganizerService;
import com.hackhub.application.service.PaymentPrizeService;
import com.hackhub.application.service.StaffAccessService;
import com.hackhub.domain.enums.HackathonStatus;
import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.Evaluation;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.Submission;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.EvaluationRepository;
import com.hackhub.infrastructure.repository.HackathonRegistrationRepository;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.SubmissionRepository;
import com.hackhub.infrastructure.repository.TeamRepository;
import com.hackhub.infrastructure.repository.UserRepository;
import com.hackhub.testsupport.TestDataFactory;
import com.hackhub.testsupport.TestSecurity;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizerServiceTest {

	@Mock
	private HackathonRepository hackathonRepository;

	@Mock
	private TeamRepository teamRepository;

	@Mock
	private HackathonRegistrationRepository registrationRepository;

	@Mock
	private SubmissionRepository submissionRepository;

	@Mock
	private EvaluationRepository evaluationRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private StaffAccessService staffAccessService;

	@Mock
	private PaymentPrizeService paymentPrizeService;

	private OrganizerService organizerService;

	@BeforeEach
	void setUp() {
		organizerService = new OrganizerService(
			hackathonRepository,
			teamRepository,
			registrationRepository,
			submissionRepository,
			evaluationRepository,
			userRepository,
			staffAccessService,
			new HackathonMapper(),
			paymentPrizeService
		);
	}

	@AfterEach
	void tearDown() {
		TestSecurity.clear();
	}

	@Test
	void declareWinnerRequiresAllSubmissionsEvaluated() {
		TestScenario scenario = prepareScenario();
		when(evaluationRepository.findBySubmission(scenario.submission())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> organizerService.declareWinner(10L, new DeclareWinnerRequest(20L)))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("All submissions must be evaluated");
	}

	@Test
	void declareWinnerSetsWinnerFinishesHackathonAndPaysPrize() {
		TestScenario scenario = prepareScenario();
		Evaluation evaluation = TestDataFactory.evaluation(40L, scenario.submission(), scenario.organizer());
		when(evaluationRepository.findBySubmission(scenario.submission())).thenReturn(Optional.of(evaluation));
		when(hackathonRepository.save(scenario.hackathon())).thenReturn(scenario.hackathon());

		var response = organizerService.declareWinner(10L, new DeclareWinnerRequest(20L));

		assertThat(response.winnerTeamId()).isEqualTo(20L);
		assertThat(response.status()).isEqualTo(HackathonStatus.FINISHED);
		assertThat(scenario.hackathon().getWinnerTeam()).isSameAs(scenario.winnerTeam());
		verify(paymentPrizeService).payWinnerPrize(scenario.hackathon(), scenario.winnerTeam());
	}

	private TestScenario prepareScenario() {
		User organizer = TestDataFactory.user(1L, Role.ORGANIZER);
		Hackathon hackathon = TestDataFactory.hackathon(10L, organizer, HackathonStatus.EVALUATION);
		Team winnerTeam = TestDataFactory.team(20L, TestDataFactory.user(2L, Role.USER), TestDataFactory.user(2L, Role.USER));
		Submission submission = TestDataFactory.submission(30L, hackathon, winnerTeam);
		TestSecurity.authenticateAs(organizer);
		when(userRepository.findByEmail(organizer.getEmail())).thenReturn(Optional.of(organizer));
		when(hackathonRepository.findById(10L)).thenReturn(Optional.of(hackathon));
		when(teamRepository.findById(20L)).thenReturn(Optional.of(winnerTeam));
		when(staffAccessService.isOrganizerOf(organizer, hackathon)).thenReturn(true);
		when(registrationRepository.existsByHackathonAndTeam(hackathon, winnerTeam)).thenReturn(true);
		when(submissionRepository.findByHackathonAndTeam(hackathon, winnerTeam)).thenReturn(Optional.of(submission));
		when(submissionRepository.findAllByHackathon(hackathon)).thenReturn(List.of(submission));
		return new TestScenario(organizer, hackathon, winnerTeam, submission);
	}

	private record TestScenario(
		User organizer,
		Hackathon hackathon,
		Team winnerTeam,
		Submission submission
	) {
	}
}
