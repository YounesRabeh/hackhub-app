package com.hackhub.application.mapper;

import com.hackhub.api.dto.response.HackathonResponse;
import com.hackhub.domain.enums.HackathonStatus;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class HackathonMapper {

	public HackathonResponse toResponse(Hackathon hackathon) {
		if (hackathon == null) {
			return null;
		}

		User organizer = MapperFieldAccess.read(hackathon, "organizer", User.class);
		Team winnerTeam = MapperFieldAccess.read(hackathon, "winnerTeam", Team.class);

		return new HackathonResponse(
			MapperFieldAccess.read(hackathon, "id", Long.class),
			MapperFieldAccess.read(hackathon, "title", String.class),
			MapperFieldAccess.read(hackathon, "description", String.class),
			MapperFieldAccess.read(hackathon, "registrationDeadline", LocalDateTime.class),
			MapperFieldAccess.read(hackathon, "submissionDeadline", LocalDateTime.class),
			MapperFieldAccess.read(hackathon, "startAt", LocalDateTime.class),
			MapperFieldAccess.read(hackathon, "endAt", LocalDateTime.class),
			MapperFieldAccess.read(hackathon, "status", HackathonStatus.class),
			MapperFieldAccess.read(hackathon, "prizeAmount", BigDecimal.class),
			organizer == null ? null : MapperFieldAccess.read(organizer, "id", Long.class),
			winnerTeam == null ? null : MapperFieldAccess.read(winnerTeam, "id", Long.class)
		);
	}
}
