package com.hackhub.unit.application.service;

import com.hackhub.api.dto.request.CreateEvaluationRequest;
import com.hackhub.api.exception.ForbiddenException;
import com.hackhub.application.service.EvaluationService;
import com.hackhub.application.service.HackathonService;
import com.hackhub.application.service.StaffAccessService;
import com.hackhub.domain.enums.HackathonStatus;
import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.Evaluation;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.Submission;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.EvaluationRepository;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.SubmissionRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

	@Mock
	private EvaluationRepository evaluationRepository;

	@Mock
	private SubmissionRepository submissionRepository;

	@Mock
	private HackathonRepository hackathonRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private StaffAccessService staffAccessService;

	@Mock
	private HackathonService hackathonService;

	private EvaluationService evaluationService;

	@BeforeEach
	void setUp() {
		evaluationService = new EvaluationService(
			evaluationRepository,
			submissionRepository,
			hackathonRepository,
			userRepository,
			staffAccessService,
			hackathonService
		);
	}

	@AfterEach
	void tearDown() {
		TestSecurity.clear();
	}

	@Test
	void evaluateSubmissionRequiresAssignedJudge() {
		User judge = TestDataFactory.user(1L, Role.JUDGE);
		Hackathon hackathon = TestDataFactory.hackathon(10L, TestDataFactory.user(2L, Role.ORGANIZER), HackathonStatus.EVALUATION);
		Submission submission = TestDataFactory.submission(30L, hackathon, TestDataFactory.team(20L, judge, judge));
		TestSecurity.authenticateAs(judge);
		when(userRepository.findByEmail(judge.getEmail())).thenReturn(Optional.of(judge));
		when(submissionRepository.findById(30L)).thenReturn(Optional.of(submission));
		when(staffAccessService.isJudgeOf(judge, hackathon)).thenReturn(false);

		assertThatThrownBy(() -> evaluationService.evaluateSubmission(30L, validEvaluationRequest()))
			.isInstanceOf(ForbiddenException.class)
			.hasMessage("Only assigned judges can evaluate submissions");
	}

	@Test
	void evaluateSubmissionCreatesEvaluationWhenMissing() {
		User judge = TestDataFactory.user(1L, Role.JUDGE);
		Hackathon hackathon = TestDataFactory.hackathon(10L, TestDataFactory.user(2L, Role.ORGANIZER), HackathonStatus.EVALUATION);
		Submission submission = TestDataFactory.submission(30L, hackathon, TestDataFactory.team(20L, judge, judge));
		TestSecurity.authenticateAs(judge);
		when(userRepository.findByEmail(judge.getEmail())).thenReturn(Optional.of(judge));
		when(submissionRepository.findById(30L)).thenReturn(Optional.of(submission));
		when(staffAccessService.isJudgeOf(judge, hackathon)).thenReturn(true);
		when(evaluationRepository.findBySubmission(submission)).thenReturn(Optional.empty());
		when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> {
			Evaluation evaluation = invocation.getArgument(0);
			evaluation.setId(40L);
			return evaluation;
		});

		var response = evaluationService.evaluateSubmission(30L, validEvaluationRequest());

		assertThat(response.id()).isEqualTo(40L);
		assertThat(response.submissionId()).isEqualTo(30L);
		assertThat(response.judgeId()).isEqualTo(1L);
		assertThat(response.score()).isEqualTo(9);
	}

	@Test
	void evaluateSubmissionUpdatesExistingEvaluation() {
		User judge = TestDataFactory.user(1L, Role.JUDGE);
		Hackathon hackathon = TestDataFactory.hackathon(10L, TestDataFactory.user(2L, Role.ORGANIZER), HackathonStatus.EVALUATION);
		Submission submission = TestDataFactory.submission(30L, hackathon, TestDataFactory.team(20L, judge, judge));
		Evaluation existingEvaluation = TestDataFactory.evaluation(40L, submission, judge);
		TestSecurity.authenticateAs(judge);
		when(userRepository.findByEmail(judge.getEmail())).thenReturn(Optional.of(judge));
		when(submissionRepository.findById(30L)).thenReturn(Optional.of(submission));
		when(staffAccessService.isJudgeOf(judge, hackathon)).thenReturn(true);
		when(evaluationRepository.findBySubmission(submission)).thenReturn(Optional.of(existingEvaluation));
		when(evaluationRepository.save(existingEvaluation)).thenReturn(existingEvaluation);

		var response = evaluationService.evaluateSubmission(
			30L,
			new CreateEvaluationRequest(6, "Updated score")
		);

		assertThat(response.id()).isEqualTo(40L);
		assertThat(response.score()).isEqualTo(6);
		assertThat(response.comment()).isEqualTo("Updated score");
	}

	private CreateEvaluationRequest validEvaluationRequest() {
		return new CreateEvaluationRequest(9, "Excellent project");
	}
}
