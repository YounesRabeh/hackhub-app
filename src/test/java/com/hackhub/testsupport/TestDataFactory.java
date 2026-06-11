package com.hackhub.testsupport;

import com.hackhub.domain.enums.HackathonStatus;
import com.hackhub.domain.enums.InvitationStatus;
import com.hackhub.domain.enums.Role;
import com.hackhub.domain.enums.SupportRequestStatus;
import com.hackhub.domain.model.Evaluation;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.HackathonRegistration;
import com.hackhub.domain.model.Submission;
import com.hackhub.domain.model.SupportRequest;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.TeamInvitation;
import com.hackhub.domain.model.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.lang.NonNull;

public final class TestDataFactory {

	private TestDataFactory() {
	}

	public static @NonNull User user(Long id, Role role) {
		User user = new User();
		user.setId(id);
		user.setEmail("user" + id + "@example.com");
		user.setPasswordHash("{noop}Password123!");
		user.setRole(role);
		return user;
	}

	public static @NonNull Hackathon hackathon(Long id, User organizer, HackathonStatus status) {
		LocalDateTime now = LocalDateTime.now();
		Hackathon hackathon = new Hackathon();
		hackathon.setId(id);
		hackathon.setTitle("Hackathon " + id);
		hackathon.setDescription("A focused test hackathon");
		hackathon.setRegistrationDeadline(now.plusDays(3));
		hackathon.setSubmissionDeadline(now.plusDays(8));
		hackathon.setStartAt(now.plusDays(4));
		hackathon.setEndAt(now.plusDays(10));
		hackathon.setStatus(status);
		hackathon.setPrizeAmount(new BigDecimal("1000.00"));
		hackathon.setOrganizer(organizer);
		return hackathon;
	}

	public static @NonNull Team team(Long id, User createdBy, User... members) {
		Team team = new Team();
		team.setId(id);
		team.setName("Team " + id);
		team.setCreatedBy(createdBy);
		for (User member : members) {
			team.getMembers().add(member);
		}
		return team;
	}

	public static @NonNull HackathonRegistration registration(
		Long id,
		Hackathon hackathon,
		Team team
	) {
		HackathonRegistration registration = new HackathonRegistration();
		registration.setId(id);
		registration.setHackathon(hackathon);
		registration.setTeam(team);
		registration.setRegisteredAt(LocalDateTime.now());
		return registration;
	}

	public static @NonNull Submission submission(Long id, Hackathon hackathon, Team team) {
		Submission submission = new Submission();
		submission.setId(id);
		submission.setHackathon(hackathon);
		submission.setTeam(team);
		submission.setProjectName("Project " + id);
		submission.setRepositoryUrl("https://github.com/example/project-" + id);
		submission.setDemoUrl("https://demo.example.com/project-" + id);
		submission.setDescription("A useful test submission");
		submission.setSubmittedAt(LocalDateTime.now().minusHours(1));
		submission.setUpdatedAt(LocalDateTime.now().minusHours(1));
		return submission;
	}

	public static @NonNull Evaluation evaluation(Long id, Submission submission, User judge) {
		Evaluation evaluation = new Evaluation();
		evaluation.setId(id);
		evaluation.setSubmission(submission);
		evaluation.setJudge(judge);
		evaluation.setScore(8);
		evaluation.setComment("Solid work");
		evaluation.setEvaluatedAt(LocalDateTime.now());
		return evaluation;
	}

	public static @NonNull TeamInvitation invitation(
		Long id,
		Team team,
		User invitedUser,
		User invitedByUser
	) {
		TeamInvitation invitation = new TeamInvitation();
		invitation.setId(id);
		invitation.setTeam(team);
		invitation.setInvitedUser(invitedUser);
		invitation.setInvitedByUser(invitedByUser);
		invitation.setStatus(InvitationStatus.PENDING);
		invitation.setCreatedAt(LocalDateTime.now());
		return invitation;
	}

	public static @NonNull SupportRequest supportRequest(
		Long id,
		Hackathon hackathon,
		Team team,
		User createdBy
	) {
		SupportRequest supportRequest = new SupportRequest();
		supportRequest.setId(id);
		supportRequest.setHackathon(hackathon);
		supportRequest.setTeam(team);
		supportRequest.setCreatedByUser(createdBy);
		supportRequest.setTitle("Need mentor help");
		supportRequest.setMessage("We need help debugging deployment.");
		supportRequest.setStatus(SupportRequestStatus.OPEN);
		supportRequest.setCreatedAt(LocalDateTime.now());
		return supportRequest;
	}
}
