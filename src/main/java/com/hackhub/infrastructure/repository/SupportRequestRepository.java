package com.hackhub.infrastructure.repository;

import com.hackhub.domain.model.SupportRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {
}
