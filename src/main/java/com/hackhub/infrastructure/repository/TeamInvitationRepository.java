package com.hackhub.infrastructure.repository;

import com.hackhub.domain.model.TeamInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {
}
