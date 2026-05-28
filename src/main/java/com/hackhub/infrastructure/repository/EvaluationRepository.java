package com.hackhub.infrastructure.repository;

import com.hackhub.domain.model.Evaluation;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.Submission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

	Optional<Evaluation> findBySubmission(Submission submission);

	List<Evaluation> findAllBySubmission_Hackathon(Hackathon hackathon);
}
