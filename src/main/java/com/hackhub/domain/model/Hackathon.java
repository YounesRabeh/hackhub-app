package com.hackhub.domain.model;

import com.hackhub.domain.enums.HackathonStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hackathons")
@Getter
@Setter
public class Hackathon {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, length = 4000)
	private String description;

	@Column(nullable = false)
	private LocalDateTime registrationDeadline;

	@Column(nullable = false)
	private LocalDateTime submissionDeadline;

	@Column(nullable = false)
	private LocalDateTime startAt;

	@Column(nullable = false)
	private LocalDateTime endAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private HackathonStatus status;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal prizeAmount;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organizer_id", nullable = false)
	private User organizer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "winner_team_id")
	private Team winnerTeam;

	@ManyToMany
	@JoinTable(
		name = "hackathon_judges",
		joinColumns = @JoinColumn(name = "hackathon_id"),
		inverseJoinColumns = @JoinColumn(name = "user_id")
	)
	private Set<User> judges = new HashSet<>();

	@ManyToMany
	@JoinTable(
		name = "hackathon_mentors",
		joinColumns = @JoinColumn(name = "hackathon_id"),
		inverseJoinColumns = @JoinColumn(name = "user_id")
	)
	private Set<User> mentors = new HashSet<>();
}
