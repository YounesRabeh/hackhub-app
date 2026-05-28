package com.hackhub.domain.model;

import com.hackhub.domain.enums.InvitationStatus;
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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
	name = "team_invitations",
	uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "invited_user_id"})
)
public class TeamInvitation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invited_user_id", nullable = false)
	private User invitedUser;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invited_by_user_id", nullable = false)
	private User invitedByUser;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private InvitationStatus status;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime respondedAt;
}
