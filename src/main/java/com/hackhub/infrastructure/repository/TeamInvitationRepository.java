package com.hackhub.infrastructure.repository;

import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.TeamInvitation;
import com.hackhub.domain.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {

	Optional<TeamInvitation> findByTeamAndInvitedUser(Team team, User invitedUser);
}
