package com.hackhub.application.mapper;

import com.hackhub.api.dto.response.SubmissionResponse;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.Submission;
import com.hackhub.domain.model.Team;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class SubmissionMapper {

	public SubmissionResponse toResponse(Submission submission) {
		if (submission == null) {
			return null;
		}

		Hackathon hackathon = MapperFieldAccess.read(submission, "hackathon", Hackathon.class);
		Team team = MapperFieldAccess.read(submission, "team", Team.class);

		return new SubmissionResponse(
			MapperFieldAccess.read(submission, "id", Long.class),
			hackathon == null ? null : MapperFieldAccess.read(hackathon, "id", Long.class),
			team == null ? null : MapperFieldAccess.read(team, "id", Long.class),
			MapperFieldAccess.read(submission, "projectName", String.class),
			MapperFieldAccess.read(submission, "repositoryUrl", String.class),
			MapperFieldAccess.read(submission, "demoUrl", String.class),
			MapperFieldAccess.read(submission, "description", String.class),
			MapperFieldAccess.read(submission, "submittedAt", LocalDateTime.class),
			MapperFieldAccess.read(submission, "updatedAt", LocalDateTime.class)
		);
	}
}
