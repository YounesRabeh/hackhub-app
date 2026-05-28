package com.hackhub.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rule_violation_reports")
@Getter
@Setter
public class RuleViolationReport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "hackathon_id", nullable = false)
	private Hackathon hackathon;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "reported_team_id", nullable = false)
	private Team reportedTeam;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "reported_by_user_id", nullable = false)
	private User reportedByUser;

	@Column(nullable = false, length = 4000)
	private String description;

	@Column(nullable = false)
	private LocalDateTime createdAt;
}
