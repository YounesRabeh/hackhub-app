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

/**
 * Factory for lightweight domain objects used by unit tests.
 *
 * <p>The objects created here are not persisted and are intentionally small:
 * each helper fills the fields needed by service and domain tests while
 * keeping relationships explicit in the method arguments. Prefer this class
 * when a test needs an in-memory aggregate. Use repository-backed setup in
 * integration tests when persistence, transactions, security, or HTTP behavior
 * matters.</p>
 */
public final class TestDataFactory {

	/**
	 * Utility class; not meant to be instantiated.
	 */
	private TestDataFactory() {
	}

	/**
	 * Creates a user with a deterministic email and role.
	 *
	 * <p>The password uses a no-op hash because these fixtures are for unit
	 * tests, not real authentication through Spring Security.</p>
	 *
	 * @param id test identifier assigned directly to the entity
	 * @param role role used by authorization-sensitive service tests
	 * @return an in-memory user fixture
	 */
	public static @NonNull User user(Long id, Role role) {
		User user = new User();
		user.setId(id);
		user.setEmail("user" + id + "@example.com");
		user.setPasswordHash("{noop}Password123!");
		user.setRole(role);
		return user;
	}

	/**
	 * Creates a hackathon with valid future dates and the requested lifecycle
	 * status.
	 *
	 * @param id test identifier assigned directly to the entity
	 * @param organizer organizer associated with the hackathon
	 * @param status lifecycle state required by the test scenario
	 * @return an in-memory hackathon fixture
	 */
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

	/**
	 * Creates a team owned by {@code createdBy} and populated with the supplied
	 * members.
	 *
	 * @param id test identifier assigned directly to the entity
	 * @param createdBy user who owns or created the team
	 * @param members users to include in the team's member collection
	 * @return an in-memory team fixture
	 */
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



	/**
	 * Creates a submission for a team's hackathon project.
	 *
	 * <p>The repository and demo URLs are syntactically valid so validation-aware
	 * service tests can reuse this fixture without additional setup.</p>
	 *
	 * @param id test identifier assigned directly to the entity
	 * @param hackathon hackathon receiving the submission
	 * @param team submitting team
	 * @return an in-memory submission fixture
	 */
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

	/**
	 * Creates an evaluation with a default passing score and comment.
	 *
	 * @param id test identifier assigned directly to the entity
	 * @param submission submission being evaluated
	 * @param judge judge who authored the evaluation
	 * @return an in-memory evaluation fixture
	 */
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

	/**
	 * Creates a pending team invitation.
	 *
	 * @param id test identifier assigned directly to the entity
	 * @param team team issuing the invitation
	 * @param invitedUser user being invited to join the team
	 * @param invitedByUser user who sent the invitation
	 * @return an in-memory invitation fixture
	 */
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

	/**
	 * Creates an open support request for a team in a hackathon.
	 *
	 * @param id test identifier assigned directly to the entity
	 * @param hackathon hackathon where help is requested
	 * @param team team requesting support
	 * @param createdBy user who opened the support request
	 * @return an in-memory support request fixture
	 */
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
