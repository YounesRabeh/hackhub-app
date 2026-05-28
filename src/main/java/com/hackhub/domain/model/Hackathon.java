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

@Entity
@Table(name = "hackathons")
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

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDateTime getRegistrationDeadline() {
		return registrationDeadline;
	}

	public void setRegistrationDeadline(LocalDateTime registrationDeadline) {
		this.registrationDeadline = registrationDeadline;
	}

	public LocalDateTime getSubmissionDeadline() {
		return submissionDeadline;
	}

	public void setSubmissionDeadline(LocalDateTime submissionDeadline) {
		this.submissionDeadline = submissionDeadline;
	}

	public LocalDateTime getStartAt() {
		return startAt;
	}

	public void setStartAt(LocalDateTime startAt) {
		this.startAt = startAt;
	}

	public LocalDateTime getEndAt() {
		return endAt;
	}

	public void setEndAt(LocalDateTime endAt) {
		this.endAt = endAt;
	}

	public HackathonStatus getStatus() {
		return status;
	}

	public void setStatus(HackathonStatus status) {
		this.status = status;
	}

	public BigDecimal getPrizeAmount() {
		return prizeAmount;
	}

	public void setPrizeAmount(BigDecimal prizeAmount) {
		this.prizeAmount = prizeAmount;
	}

	public User getOrganizer() {
		return organizer;
	}

	public void setOrganizer(User organizer) {
		this.organizer = organizer;
	}

	public Team getWinnerTeam() {
		return winnerTeam;
	}

	public void setWinnerTeam(Team winnerTeam) {
		this.winnerTeam = winnerTeam;
	}

	public Set<User> getJudges() {
		return judges;
	}

	public Set<User> getMentors() {
		return mentors;
	}
}
