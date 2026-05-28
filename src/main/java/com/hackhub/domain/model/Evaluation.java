package com.hackhub.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "evaluations", uniqueConstraints = @UniqueConstraint(columnNames = {"submission_id"}))
@Getter
@Setter
public class Evaluation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "submission_id", nullable = false, unique = true)
	private Submission submission;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "judge_id", nullable = false)
	private User judge;

	@Column(nullable = false)
	private Integer score;

	@Column(nullable = false, length = 4000)
	private String comment;

	@Column(nullable = false)
	private LocalDateTime evaluatedAt;
}
