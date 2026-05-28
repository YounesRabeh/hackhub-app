package com.hackhub.infrastructure.repository;

import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.HackathonRegistration;
import com.hackhub.domain.model.Team;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HackathonRegistrationRepository extends JpaRepository<HackathonRegistration, Long> {

	boolean existsByHackathonAndTeam(Hackathon hackathon, Team team);

	List<HackathonRegistration> findAllByHackathon(Hackathon hackathon);
}
