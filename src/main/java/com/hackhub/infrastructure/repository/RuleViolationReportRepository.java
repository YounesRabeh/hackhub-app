package com.hackhub.infrastructure.repository;

import com.hackhub.domain.model.RuleViolationReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleViolationReportRepository extends JpaRepository<RuleViolationReport, Long> {
}
