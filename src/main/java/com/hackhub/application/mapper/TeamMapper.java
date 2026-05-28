package com.hackhub.application.mapper;

import com.hackhub.api.dto.response.TeamResponse;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {

	@SuppressWarnings("unchecked")
	public TeamResponse toResponse(Team team) {
		if (team == null) {
			return null;
		}

		User createdBy = MapperFieldAccess.read(team, "createdBy", User.class);
		Set<User> members = MapperFieldAccess.read(team, "members", Set.class);

		List<Long> memberIds = members == null
			? List.of()
			: members.stream().map(member -> MapperFieldAccess.read(member, "id", Long.class)).toList();

		return new TeamResponse(
			MapperFieldAccess.read(team, "id", Long.class),
			MapperFieldAccess.read(team, "name", String.class),
			createdBy == null ? null : MapperFieldAccess.read(createdBy, "id", Long.class),
			memberIds
		);
	}
}
