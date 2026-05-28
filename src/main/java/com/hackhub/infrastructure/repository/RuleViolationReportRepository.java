package com.hackhub.infrastructure.repository;

import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.RuleViolationReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleViolationReportRepository extends JpaRepository<RuleViolationReport, Long> {

	List<RuleViolationReport> findAllByHackathon(Hackathon hackathon);
}
