package com.hackhub.infrastructure.repository;

import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.SupportRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {

	List<SupportRequest> findAllByHackathon(Hackathon hackathon);
}
