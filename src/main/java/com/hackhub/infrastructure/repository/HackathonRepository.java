package com.hackhub.infrastructure.repository;

import com.hackhub.domain.model.Hackathon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HackathonRepository extends JpaRepository<Hackathon, Long> {
}
