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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
	name = "submissions",
	uniqueConstraints = @UniqueConstraint(columnNames = {"hackathon_id", "team_id"})
)
@Getter
@Setter
public class Submission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "hackathon_id", nullable = false)
	private Hackathon hackathon;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@Column(nullable = false)
	private String projectName;

	@Column(nullable = false)
	private String repositoryUrl;

	private String demoUrl;

	@Column(nullable = false, length = 4000)
	private String description;

	@Column(nullable = false)
	private LocalDateTime submittedAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;
}
