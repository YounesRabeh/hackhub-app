package com.hackhub.integration.workflow;

import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.User;
import com.hackhub.testsupport.IntegrationTestSupport;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the main hackathon HTTP workflow.
 *
 * <p>This test verifies the complete flow from hackathon creation,
 * team creation, invitation acceptance, registration, judge assignment,
 * submission, evaluation, and winner selection.</p>
 */
class HackathonHttpFlowIntegrationTest extends IntegrationTestSupport {

	/**
	 * Verifies that the main hackathon flow works correctly through HTTP endpoints.
	*/
	@Test
	void mainHackathonFlowWorksThroughHttp() throws Exception {
		User organizer = saveUser(Role.ORGANIZER);
		User judge = saveUser(Role.JUDGE);
		User user1 = saveUser(Role.USER);
		User user2 = saveUser(Role.USER);
		String organizerToken = tokenFor(organizer);
		String judgeToken = tokenFor(judge);
		String user1Token = tokenFor(user1);
		String user2Token = tokenFor(user2);

		get("/api/hackathons")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray());

		MvcResult hackathonResult = postJsonWithBearer(
			"/api/hackathons",
			organizerToken,
			validHackathonPayload()
		)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("REGISTRATION_OPEN"))
			.andReturn();
		Long hackathonId = idFrom(hackathonResult);

		MvcResult teamResult = postJsonWithBearer(
			"/api/teams",
			user1Token,
			createTeamPayload("CodeStorm")
		)
			.andExpect(status().isCreated())
			.andReturn();
		Long teamId = idFrom(teamResult);

		MvcResult invitationResult = postJsonWithBearer(
			"/api/teams/" + teamId + "/invitations",
			user1Token,
			createInvitationPayload(user2.getId())
		)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("PENDING"))
			.andReturn();
		Long invitationId = idFrom(invitationResult);

		postJsonWithBearer("/api/invitations/" + invitationId + "/accept", user2Token, Map.of())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("ACCEPTED"));

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/registrations",
			user1Token,
			registerTeamPayload(teamId)
		)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.teamId").value(teamId));

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/judges/" + judge.getId(),
			organizerToken,
			Map.of()
		).andExpect(status().isOk());

		patchJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/status",
			organizerToken,
			Map.of("status", "IN_PROGRESS")
		).andExpect(status().isOk());

		MvcResult submissionResult = putJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/submissions/my-team",
			user1Token,
			submissionPayload()
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.teamId").value(teamId))
			.andReturn();
		Long submissionId = idFrom(submissionResult);

		patchJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/status",
			organizerToken,
			Map.of("status", "EVALUATION")
		).andExpect(status().isOk());

		postJsonWithBearer(
			"/api/submissions/" + submissionId + "/evaluation",
			judgeToken,
			evaluationPayload(9)
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.submissionId").value(submissionId))
			.andExpect(jsonPath("$.score").value(9));

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/winner",
			organizerToken,
			winnerPayload(teamId)
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.winnerTeamId").value(teamId))
			.andExpect(jsonPath("$.status").value("FINISHED"));
	}

	/**
	 * Verifies that unauthenticated and unauthorized users are rejected.
	*/
	@Test
	void securityRejectsUnauthorizedAndForbiddenRequests() throws Exception {
		User user = saveUser(Role.USER);
		String userToken = tokenFor(user);

		postJson("/api/hackathons", validHackathonPayload())
			.andExpect(status().isUnauthorized());

		postJsonWithBearer("/api/hackathons", userToken, validHackathonPayload())
			.andExpect(status().isForbidden());

		get("/api/hackathons/999/submissions")
			.andExpect(status().isUnauthorized());
	}

	/**
	 * Verifies that only users with the JUDGE role can be assigned as judges.
	*/
	@Test
	void judgeAssignmentRejectsUsersWithoutJudgeRole() throws Exception {
		User organizer = saveUser(Role.ORGANIZER);
		User user = saveUser(Role.USER);
		String organizerToken = tokenFor(organizer);

		MvcResult hackathonResult = postJsonWithBearer(
			"/api/hackathons",
			organizerToken,
			validHackathonPayload()
		)
			.andExpect(status().isCreated())
			.andReturn();
		Long hackathonId = idFrom(hackathonResult);

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/judges/" + user.getId(),
			organizerToken,
			Map.of()
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Assigned user must have role JUDGE"));
	}

	@Test
	void invitationFlowRejectsNonTeamMemberInviter() throws Exception {
		User teamMember = saveUser(Role.USER);
		User nonMember = saveUser(Role.USER);
		User invitedUser = saveUser(Role.USER);
		String teamMemberToken = tokenFor(teamMember);
		String nonMemberToken = tokenFor(nonMember);
		Long teamId = createTeam(teamMemberToken, "Non Member Invite Team");

		postJsonWithBearer(
			"/api/teams/" + teamId + "/invitations",
			nonMemberToken,
			createInvitationPayload(invitedUser.getId())
		)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.message").value("Only team members can invite users"));
	}

	@Test
	void invitationFlowRejectsInvitingUserAlreadyOnTeam() throws Exception {
		User inviter = saveUser(Role.USER);
		User invitedUser = saveUser(Role.USER);
		String inviterToken = tokenFor(inviter);
		String invitedUserToken = tokenFor(invitedUser);
		Long teamId = createTeam(inviterToken, "Already On Team Inviter");
		createTeam(invitedUserToken, "Already On Team Invitee");

		postJsonWithBearer(
			"/api/teams/" + teamId + "/invitations",
			inviterToken,
			createInvitationPayload(invitedUser.getId())
		)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value("Invited user already belongs to a team"));
	}

	@Test
	void invitationFlowRejectsDuplicateInvitation() throws Exception {
		User inviter = saveUser(Role.USER);
		User invitedUser = saveUser(Role.USER);
		String inviterToken = tokenFor(inviter);
		Long teamId = createTeam(inviterToken, "Duplicate Invite Team");

		createInvitation(teamId, inviterToken, invitedUser.getId());

		postJsonWithBearer(
			"/api/teams/" + teamId + "/invitations",
			inviterToken,
			createInvitationPayload(invitedUser.getId())
		)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value("Invitation already exists for this user and team"));
	}

	@Test
	void nonInviteeCannotAcceptOrDeclineInvitation() throws Exception {
		User inviter = saveUser(Role.USER);
		User invitedUser = saveUser(Role.USER);
		User otherUser = saveUser(Role.USER);
		String inviterToken = tokenFor(inviter);
		String otherUserToken = tokenFor(otherUser);
		Long teamId = createTeam(inviterToken, "Non Invitee Team");
		Long invitationId = createInvitation(teamId, inviterToken, invitedUser.getId());

		postJsonWithBearer("/api/invitations/" + invitationId + "/accept", otherUserToken, Map.of())
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.message").value("Only invited user can respond to this invitation"));

		postJsonWithBearer("/api/invitations/" + invitationId + "/decline", otherUserToken, Map.of())
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.message").value("Only invited user can respond to this invitation"));
	}

	@Test
	void answeredInvitationCannotBeAnsweredAgain() throws Exception {
		User inviter = saveUser(Role.USER);
		User acceptedInvitee = saveUser(Role.USER);
		User declinedInvitee = saveUser(Role.USER);
		String inviterToken = tokenFor(inviter);
		String acceptedInviteeToken = tokenFor(acceptedInvitee);
		String declinedInviteeToken = tokenFor(declinedInvitee);
		Long teamId = createTeam(inviterToken, "Answered Invite Team");
		Long acceptedInvitationId = createInvitation(teamId, inviterToken, acceptedInvitee.getId());
		Long declinedInvitationId = createInvitation(teamId, inviterToken, declinedInvitee.getId());

		postJsonWithBearer(
			"/api/invitations/" + acceptedInvitationId + "/accept",
			acceptedInviteeToken,
			Map.of()
		).andExpect(status().isOk());

		postJsonWithBearer(
			"/api/invitations/" + acceptedInvitationId + "/decline",
			acceptedInviteeToken,
			Map.of()
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Invitation has already been answered"));

		postJsonWithBearer(
			"/api/invitations/" + declinedInvitationId + "/decline",
			declinedInviteeToken,
			Map.of()
		).andExpect(status().isOk());

		postJsonWithBearer(
			"/api/invitations/" + declinedInvitationId + "/accept",
			declinedInviteeToken,
			Map.of()
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Invitation has already been answered"));
	}

	@Test
	void teamRegistrationRejectsCurrentUserWhoIsNotTeamMember() throws Exception {
		User organizer = saveUser(Role.ORGANIZER);
		User teamMember = saveUser(Role.USER);
		User nonMember = saveUser(Role.USER);
		String organizerToken = tokenFor(organizer);
		String teamMemberToken = tokenFor(teamMember);
		String nonMemberToken = tokenFor(nonMember);
		Long hackathonId = createHackathon(organizerToken);
		Long teamId = createTeam(teamMemberToken, "Registration Non Member Team");

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/registrations",
			nonMemberToken,
			registerTeamPayload(teamId)
		)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.message").value("Only team members can register their team"));
	}

	@Test
	void teamRegistrationRejectsAlreadyRegisteredTeam() throws Exception {
		User organizer = saveUser(Role.ORGANIZER);
		User teamMember = saveUser(Role.USER);
		String organizerToken = tokenFor(organizer);
		String teamMemberToken = tokenFor(teamMember);
		Long hackathonId = createHackathon(organizerToken);
		Long teamId = createTeam(teamMemberToken, "Already Registered Team");

		registerTeam(hackathonId, teamMemberToken, teamId);

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/registrations",
			teamMemberToken,
			registerTeamPayload(teamId)
		)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value("Team is already registered to this hackathon"));
	}

	@Test
	void teamRegistrationRejectsTeamOverMaxSize() throws Exception {
		User organizer = saveUser(Role.ORGANIZER);
		User owner = saveUser(Role.USER);
		String organizerToken = tokenFor(organizer);
		String ownerToken = tokenFor(owner);
		Long hackathonId = createHackathon(organizerToken);
		Long teamId = createTeam(ownerToken, "Oversized Team");

		for (int i = 0; i < 5; i++) {
			User invitee = saveUser(Role.USER);
			String inviteeToken = tokenFor(invitee);
			Long invitationId = createInvitation(teamId, ownerToken, invitee.getId());
			postJsonWithBearer("/api/invitations/" + invitationId + "/accept", inviteeToken, Map.of())
				.andExpect(status().isOk());
		}

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/registrations",
			ownerToken,
			registerTeamPayload(teamId)
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Team exceeds maximum allowed size"));
	}

	@Test
	void submissionUpsertUpdatesExistingSubmission() throws Exception {
		User organizer = saveUser(Role.ORGANIZER);
		User teamMember = saveUser(Role.USER);
		String organizerToken = tokenFor(organizer);
		String teamMemberToken = tokenFor(teamMember);
		Long hackathonId = createHackathon(organizerToken);
		Long teamId = createTeam(teamMemberToken, "Submission Update Team");
		registerTeam(hackathonId, teamMemberToken, teamId);
		moveHackathonToInProgress(hackathonId, organizerToken);

		MvcResult firstSubmission = putJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/submissions/my-team",
			teamMemberToken,
			submissionPayload()
		)
			.andExpect(status().isOk())
			.andReturn();
		Long submissionId = idFrom(firstSubmission);

		putJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/submissions/my-team",
			teamMemberToken,
			updatedSubmissionPayload()
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(submissionId))
			.andExpect(jsonPath("$.teamId").value(teamId))
			.andExpect(jsonPath("$.projectName").value("AI Study Buddy v2"))
			.andExpect(jsonPath("$.repositoryUrl").value("https://github.com/hackhub/ai-study-buddy-v2"))
			.andExpect(jsonPath("$.demoUrl").value("https://demo.hackhub.app/study-buddy-v2"))
			.andExpect(jsonPath("$.description").value("An updated study planning assistant"));
	}

	@Test
	void ruleViolationReportRejectsMentorWithoutAccess() throws Exception {
		User organizer = saveUser(Role.ORGANIZER);
		User mentor = saveUser(Role.MENTOR);
		User teamMember = saveUser(Role.USER);
		String organizerToken = tokenFor(organizer);
		String mentorToken = tokenFor(mentor);
		String teamMemberToken = tokenFor(teamMember);
		Long hackathonId = createHackathon(organizerToken);
		Long teamId = createTeam(teamMemberToken, "Unassigned Mentor Report Team");
		registerTeam(hackathonId, teamMemberToken, teamId);

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/rule-violations",
			mentorToken,
			reportViolationPayload(teamId)
		)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.message").value("Only assigned mentors can report violations"));
	}

	@Test
	void ruleViolationReportRejectsUnregisteredReportedTeam() throws Exception {
		User organizer = saveUser(Role.ORGANIZER);
		User mentor = saveUser(Role.MENTOR);
		User teamMember = saveUser(Role.USER);
		String organizerToken = tokenFor(organizer);
		String mentorToken = tokenFor(mentor);
		String teamMemberToken = tokenFor(teamMember);
		Long hackathonId = createHackathon(organizerToken);
		Long teamId = createTeam(teamMemberToken, "Unregistered Report Team");

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/mentors/" + mentor.getId(),
			organizerToken,
			Map.of()
		).andExpect(status().isOk());

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/rule-violations",
			mentorToken,
			reportViolationPayload(teamId)
		)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.message").value("Reported team must be registered to hackathon"));
	}

	private Long createHackathon(String organizerToken) throws Exception {
		MvcResult hackathonResult = postJsonWithBearer(
			"/api/hackathons",
			organizerToken,
			validHackathonPayload()
		)
			.andExpect(status().isCreated())
			.andReturn();
		return idFrom(hackathonResult);
	}

	private Long createTeam(String userToken, String name) throws Exception {
		MvcResult teamResult = postJsonWithBearer(
			"/api/teams",
			userToken,
			createTeamPayload(name)
		)
			.andExpect(status().isCreated())
			.andReturn();
		return idFrom(teamResult);
	}

	private Long createInvitation(
		Long teamId,
		String inviterToken,
		Long invitedUserId
	) throws Exception {
		MvcResult invitationResult = postJsonWithBearer(
			"/api/teams/" + teamId + "/invitations",
			inviterToken,
			createInvitationPayload(invitedUserId)
		)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("PENDING"))
			.andReturn();
		return idFrom(invitationResult);
	}

	private void registerTeam(
		Long hackathonId,
		String teamMemberToken,
		Long teamId
	) throws Exception {
		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/registrations",
			teamMemberToken,
			registerTeamPayload(teamId)
		)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.teamId").value(teamId));
	}

	private void moveHackathonToInProgress(Long hackathonId, String organizerToken) throws Exception {
		patchJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/status",
			organizerToken,
			Map.of("status", "IN_PROGRESS")
		).andExpect(status().isOk());
	}

	private Map<String, Object> updatedSubmissionPayload() {
		return Map.of(
			"projectName", "AI Study Buddy v2",
			"repositoryUrl", "https://github.com/hackhub/ai-study-buddy-v2",
			"demoUrl", "https://demo.hackhub.app/study-buddy-v2",
			"description", "An updated study planning assistant"
		);
	}

	private Map<String, Object> reportViolationPayload(Long teamId) {
		return Map.of(
			"reportedTeamId", teamId,
			"description", "Suspicious submission activity"
		);
	}

	private Map<String, Object> validHackathonPayload() {
		LocalDateTime now = LocalDateTime.now().plusDays(3);
		return Map.of(
			"title", "HackHub Integration",
			"description", "A complete integration test hackathon",
			"registrationDeadline", now.plusDays(1),
			"submissionDeadline", now.plusDays(5),
			"startAt", now.plusDays(2),
			"endAt", now.plusDays(7),
			"prizeAmount", new BigDecimal("5000.00")
		);
	}
}
