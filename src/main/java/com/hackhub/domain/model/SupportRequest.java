package com.hackhub.domain.model;

import com.hackhub.domain.enums.SupportRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_requests")
public class SupportRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "hackathon_id", nullable = false)
	private Hackathon hackathon;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by_user_id", nullable = false)
	private User createdByUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_mentor_id")
	private User assignedMentor;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, length = 4000)
	private String message;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SupportRequestStatus status;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime closedAt;
}
