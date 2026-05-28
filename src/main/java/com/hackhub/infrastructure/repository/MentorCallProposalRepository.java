package com.hackhub.infrastructure.repository;

import com.hackhub.domain.model.MentorCallProposal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorCallProposalRepository extends JpaRepository<MentorCallProposal, Long> {
}
