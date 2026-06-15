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
import org.springframework.lang.NonNull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EvaluationService}.
 *
 * <p>The service is tested with mocked repositories and collaborators so each
 * case can focus on evaluation-specific behavior: assigned-judge authorization,
 * creating the first evaluation for a submission, and updating the existing
 * evaluation when one is already present.</p>
 *
 * <p>Authentication is supplied through {@link TestSecurity}, which populates
 * Spring Security's context with the fixture judge user for each scenario.</p>
 */
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

	/**
	 * Creates the service under test with mocked persistence and authorization
	 * collaborators.
	 */
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

	/**
	 * Clears the static Spring Security context after each test so authenticated
	 * users cannot leak between scenarios.
	 */
	@AfterEach
	void tearDown() {
		TestSecurity.clear();
	}

	/**
	 * Verifies that a judge cannot evaluate a submission unless they are
	 * assigned to the submission's hackathon.
	 */
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

	/**
	 * Verifies that the service creates and saves a new evaluation when the
	 * submission has not been evaluated yet.
	 */
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
		Evaluation savedEvaluation = TestDataFactory.evaluation(40L, submission, judge);
		savedEvaluation.setScore(9);
		when(evaluationRepository.save(anyEvaluation())).thenReturn(savedEvaluation);

		var response = evaluationService.evaluateSubmission(30L, validEvaluationRequest());

		assertThat(response.id()).isEqualTo(40L);
		assertThat(response.submissionId()).isEqualTo(30L);
		assertThat(response.judgeId()).isEqualTo(1L);
		assertThat(response.score()).isEqualTo(9);
	}

	/**
	 * Verifies that the service updates the existing evaluation instead of
	 * creating a duplicate evaluation for the same submission.
	 */
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

	/**
	 * Builds the standard valid evaluation request used by positive scenarios.
	 */
	private @NonNull CreateEvaluationRequest validEvaluationRequest() {
		return new CreateEvaluationRequest(9, "Excellent project");
	}

	/**
	 * Mockito matcher for repository saves that accept any {@link Evaluation}.
	 */
	@SuppressWarnings("all")
	private static @NonNull Evaluation anyEvaluation() {
		return isA(Evaluation.class);
	}
}
