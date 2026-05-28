package com.hackhub.infrastructure.repository;

import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.Submission;
import com.hackhub.domain.model.Team;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

	Optional<Submission> findByHackathonAndTeam(Hackathon hackathon, Team team);
}
