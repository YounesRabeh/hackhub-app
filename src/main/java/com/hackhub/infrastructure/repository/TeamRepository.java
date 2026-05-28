package com.hackhub.infrastructure.repository;

import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

	Optional<Team> findByMembersContaining(User user);

	boolean existsByIdAndMembersContaining(Long teamId, User user);
}
