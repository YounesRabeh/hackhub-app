package com.hackhub.integration.coverage;

import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.User;
import com.hackhub.testsupport.IntegrationTestSupport;
import com.hackhub.testsupport.coverage.EndpointCoverageRegistry;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(OrderAnnotation.class)
class EndpointCoverageIntegrationTest extends IntegrationTestSupport {

	private static final String CONTROLLER_PACKAGE_PREFIX = "com.hackhub.api.controller";
	private static final double DEFAULT_THRESHOLD_PERCENT = 80.0;
	private static final Logger LOGGER = LoggerFactory.getLogger(EndpointCoverageIntegrationTest.class);

	@Autowired
	private EndpointCoverageRegistry endpointCoverageRegistry;

	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Test
	@Order(1)
	void coverAuthTeamAndInvitationEndpoints() throws Exception {
		String emailOne = "coverage+" + System.nanoTime() + "@example.com";
		String emailTwo = "coverage+" + (System.nanoTime() + 1) + "@example.com";

		MvcResult registerOne = postJson("/api/auth/register", registerPayload(emailOne, PASSWORD))
			.andExpect(status().isCreated())
			.andReturn();
		postJson("/api/auth/register", registerPayload(emailTwo, PASSWORD))
			.andExpect(status().isCreated());

		String tokenOne = extractToken(registerOne);
		MvcResult loginOne = postJson(
			"/api/auth/login",
			loginPayload(emailOne.toUpperCase(Locale.ROOT), PASSWORD)
		)
			.andExpect(status().isOk())
			.andReturn();
		String tokenTwo = extractToken(
			postJson("/api/auth/login", loginPayload(emailTwo, PASSWORD))
				.andExpect(status().isOk())
				.andReturn()
		);

		getWithBearer("/api/auth/me", extractToken(loginOne))
			.andExpect(status().isOk());

		MvcResult team = postJsonWithBearer("/api/teams", tokenOne, createTeamPayload("Coverage Team"))
			.andExpect(status().isCreated())
			.andReturn();
		Long teamId = idFrom(team);

		getWithBearer("/api/teams/me", tokenOne)
			.andExpect(status().isOk());

		MvcResult invitationOne = postJsonWithBearer(
			"/api/teams/" + teamId + "/invitations",
			tokenOne,
			createInvitationPayload(
				userRepository.findByEmail(emailTwo).orElseThrow().getId()
			)
		)
			.andExpect(status().isCreated())
			.andReturn();

		postJsonWithBearer(
			"/api/invitations/" + idFrom(invitationOne) + "/decline",
			tokenTwo,
			Map.of()
		).andExpect(status().isOk());

		String emailThree = "coverage+" + (System.nanoTime() + 2) + "@example.com";
		postJson("/api/auth/register", registerPayload(emailThree, PASSWORD))
			.andExpect(status().isCreated());
		String tokenThree = extractToken(
			postJson("/api/auth/login", loginPayload(emailThree, PASSWORD))
				.andExpect(status().isOk())
				.andReturn()
		);
		Long userThreeId = userRepository.findByEmail(emailThree).orElseThrow().getId();

		MvcResult invitationTwo = postJsonWithBearer(
			"/api/teams/" + teamId + "/invitations",
			tokenOne,
			createInvitationPayload(userThreeId)
		)
			.andExpect(status().isCreated())
			.andReturn();

		postJsonWithBearer(
			"/api/invitations/" + idFrom(invitationTwo) + "/accept",
			tokenThree,
			Map.of()
		).andExpect(status().isOk());
	}

	@Test
	@Order(2)
	void coverHackathonAndEvaluationEndpoints() throws Exception {
		User organizer = saveUser(Role.ORGANIZER);
		User mentor = saveUser(Role.MENTOR);
		User judge = saveUser(Role.JUDGE);
		User participant = saveUser(Role.USER);

		String organizerToken = tokenFor(organizer);
		String mentorToken = tokenFor(mentor);
		String judgeToken = tokenFor(judge);
		String participantToken = tokenFor(participant);

		MvcResult team = postJsonWithBearer(
			"/api/teams",
			participantToken,
			createTeamPayload("Flow Team")
		)
			.andExpect(status().isCreated())
			.andReturn();
		Long teamId = idFrom(team);

		get("/api/hackathons")
			.andExpect(status().isOk());

		MvcResult hackathon = postJsonWithBearer(
			"/api/hackathons",
			organizerToken,
			validHackathonPayload()
		)
			.andExpect(status().isCreated())
			.andReturn();
		Long hackathonId = idFrom(hackathon);

		get("/api/hackathons/" + hackathonId)
			.andExpect(status().isOk());

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/mentors/" + mentor.getId(),
			organizerToken,
			Map.of()
		).andExpect(status().isOk());

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/judges/" + judge.getId(),
			organizerToken,
			Map.of()
		).andExpect(status().isOk());

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/registrations",
			participantToken,
			registerTeamPayload(teamId)
		)
			.andExpect(status().isCreated());

		getWithBearer("/api/hackathons/" + hackathonId + "/registrations", organizerToken)
			.andExpect(status().isOk());

		patchJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/status",
			organizerToken,
			Map.of("status", "IN_PROGRESS")
		).andExpect(status().isOk());

		MvcResult submission = putJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/submissions/my-team",
			participantToken,
			submissionPayload()
		)
			.andExpect(status().isOk())
			.andReturn();
		Long submissionId = idFrom(submission);

		getWithBearer("/api/hackathons/" + hackathonId + "/submissions/my-team", participantToken)
			.andExpect(status().isOk());
		getWithBearer("/api/hackathons/" + hackathonId + "/submissions", organizerToken)
			.andExpect(status().isOk());

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/support-requests",
			participantToken,
			Map.of("title", "Need backend help", "message", "Auth filter blocks us")
		)
			.andExpect(status().isCreated());

		getWithBearer("/api/hackathons/" + hackathonId + "/support-requests", mentorToken)
			.andExpect(status().isOk());

		Long supportRequestId = objectMapper.readTree(
			getWithBearer("/api/hackathons/" + hackathonId + "/support-requests", mentorToken)
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString()
		).get(0).path("id").asLong();

		postJsonWithBearer(
			"/api/support-requests/" + supportRequestId + "/call-proposal",
			mentorToken,
			Map.of("scheduledAt", LocalDateTime.now().plusDays(2))
		)
			.andExpect(status().isForbidden());

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/rule-violations",
			mentorToken,
			Map.of(
				"reportedTeamId", teamId,
				"description", "Potential rule violation for testing endpoint coverage"
			)
		)
			.andExpect(status().isCreated());

		getWithBearer("/api/hackathons/" + hackathonId + "/rule-violations", organizerToken)
			.andExpect(status().isOk());

		patchJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/status",
			organizerToken,
			Map.of("status", "EVALUATION")
		).andExpect(status().isOk());

		postJsonWithBearer(
			"/api/submissions/" + submissionId + "/evaluation",
			judgeToken,
			evaluationPayload(9)
		).andExpect(status().isOk());

		getWithBearer("/api/hackathons/" + hackathonId + "/evaluations", organizerToken)
			.andExpect(status().isOk());

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/winner",
			organizerToken,
			winnerPayload(teamId)
		).andExpect(status().isOk());
	}

	@Test
	@Order(99)
	void shouldPrintEndpointCoverageAndEnforceThreshold() {
		Set<String> allControllerEndpoints = discoverAllControllerEndpoints();
		Set<String> hitEndpoints = endpointCoverageRegistry.snapshotHitEndpoints();

		int total = allControllerEndpoints.size();
		int hit = (int) allControllerEndpoints.stream().filter(hitEndpoints::contains).count();
		double coveragePercent = total == 0 ? 100.0 : (hit * 100.0) / total;
		double thresholdPercent = endpointCoverageThresholdPercent();

		Set<String> missingEndpoints = new TreeSet<>(allControllerEndpoints);
		missingEndpoints.removeAll(hitEndpoints);

		LOGGER.info(
			"Endpoint coverage: {}/{} ({}) - threshold {}%",
			hit,
			total,
			String.format("%.2f%%", coveragePercent),
			thresholdPercent
		);
		if (!missingEndpoints.isEmpty()) {
			LOGGER.info("Missing endpoints: {}", missingEndpoints);
		}

		assertTrue(
			coveragePercent >= thresholdPercent,
			"Endpoint coverage below threshold: " + hit + "/" + total +
				" (" + String.format("%.2f", coveragePercent) + "%)"
		);
	}

	private Set<String> discoverAllControllerEndpoints() {
		Set<String> endpoints = new TreeSet<>();

		for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : requestMappingHandlerMapping
			.getHandlerMethods()
			.entrySet()) {
			HandlerMethod handlerMethod = entry.getValue();
			if (!handlerMethod.getBeanType().getPackageName().startsWith(CONTROLLER_PACKAGE_PREFIX)) {
				continue;
			}

			RequestMappingInfo mappingInfo = entry.getKey();
			Set<String> paths = mappingInfo.getPatternValues();
			Set<String> methods = mappingInfo
				.getMethodsCondition()
				.getMethods()
				.stream()
				.map(RequestMethod::name)
				.collect(java.util.stream.Collectors.toSet());

			for (String path : paths) {
				for (String method : methods) {
					endpoints.add(method + " " + path);
				}
			}
		}

		return endpoints;
	}

	private double endpointCoverageThresholdPercent() {
		String value = System.getProperty(
			"endpoint.coverage.threshold",
			String.valueOf(DEFAULT_THRESHOLD_PERCENT)
		);
		return Double.parseDouble(value);
	}

	private Map<String, Object> validHackathonPayload() {
		LocalDateTime now = LocalDateTime.now().plusDays(3);
		return Map.of(
			"title", "Endpoint Coverage Hackathon",
			"description", "Covers all major hackathon endpoints",
			"registrationDeadline", now.plusDays(1),
			"submissionDeadline", now.plusDays(5),
			"startAt", now.plusDays(2),
			"endAt", now.plusDays(7),
			"prizeAmount", new BigDecimal("1000.00")
		);
	}
}
