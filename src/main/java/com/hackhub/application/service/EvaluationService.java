package com.hackhub.application.service;

import com.hackhub.api.dto.request.CreateEvaluationRequest;
import com.hackhub.api.dto.response.EvaluationResponse;
import com.hackhub.api.exception.ForbiddenException;
import com.hackhub.api.exception.NotFoundException;
import com.hackhub.domain.model.Evaluation;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.Submission;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.EvaluationRepository;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.SubmissionRepository;
import com.hackhub.infrastructure.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EvaluationService {

	private final EvaluationRepository evaluationRepository;
	private final SubmissionRepository submissionRepository;
	private final HackathonRepository hackathonRepository;
	private final UserRepository userRepository;
	private final StaffAccessService staffAccessService;
	private final HackathonService hackathonService;

	@Transactional
	public EvaluationResponse evaluateSubmission(
		Long submissionId,
		CreateEvaluationRequest request
	) {
		User currentUser = currentUser();
		Submission submission = submissionRepository
			.findById(submissionId)
			.orElseThrow(() -> new NotFoundException("Submission not found"));
		Hackathon hackathon = submission.getHackathon();

		if (!staffAccessService.isJudgeOf(currentUser, hackathon)) {
			throw new ForbiddenException("Only assigned judges can evaluate submissions");
		}

		hackathonService.assertEvaluationAllowed(hackathon);

		Evaluation evaluation = evaluationRepository
			.findBySubmission(submission)
			.orElseGet(Evaluation::new);

		evaluation.setSubmission(submission);
		evaluation.setJudge(currentUser);
		evaluation.setScore(request.score());
		evaluation.setComment(request.comment().trim());
		evaluation.setEvaluatedAt(LocalDateTime.now());

		return toResponse(evaluationRepository.save(evaluation));
	}

	@Transactional(readOnly = true)
	public List<EvaluationResponse> listHackathonEvaluations(Long hackathonId) {
		User currentUser = currentUser();
		Hackathon hackathon = hackathonRepository
			.findById(hackathonId)
			.orElseThrow(() -> new NotFoundException("Hackathon not found"));

		if (!staffAccessService.canAccessSubmissions(currentUser, hackathon)) {
			throw new ForbiddenException("Only assigned staff can view evaluations");
		}

		return evaluationRepository
			.findAllBySubmission_Hackathon(hackathon)
			.stream()
			.map(this::toResponse)
			.toList();
	}

	private User currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			throw new NotFoundException("Authenticated user not found");
		}

		return userRepository
			.findByEmail(authentication.getName())
			.orElseThrow(() -> new NotFoundException("Authenticated user not found"));
	}

	private EvaluationResponse toResponse(Evaluation evaluation) {
		return new EvaluationResponse(
			evaluation.getId(),
			evaluation.getSubmission().getId(),
			evaluation.getJudge().getId(),
			evaluation.getScore(),
			evaluation.getComment(),
			evaluation.getEvaluatedAt()
		);
	}
}
