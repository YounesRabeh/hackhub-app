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
