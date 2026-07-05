package com.hackhub.testsupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.Team;
import com.hackhub.domain.model.User;
import com.hackhub.testsupport.coverage.EndpointCoverageTestConfiguration;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.TeamRepository;
import com.hackhub.infrastructure.repository.UserRepository;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for full-stack HTTP integration tests.
 *
 * <p>Subclasses run with the complete Spring Boot application context,
 * MockMvc, repositories, JSON serialization, security, and the endpoint
 * coverage interceptor enabled. The helper methods keep integration tests
 * focused on user flows rather than repeated request-building and fixture
 * setup code.</p>
 *
 * <p>Every test method runs inside a transaction that rolls back by default,
 * so generated users, teams, hackathons, and related records do not leak into
 * other integration tests.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(EndpointCoverageTestConfiguration.class)
@SuppressWarnings("null")
public abstract class IntegrationTestSupport {

	/**
	 * Shared password for test users created through this support class.
	 */
	protected static final String PASSWORD = "Password123!";

	/**
	 * MockMvc client wired against the real Spring MVC and security stack.
	 */
	@Autowired
	protected MockMvc mockMvc;

	/**
	 * Application ObjectMapper used to serialize requests and inspect responses.
	 */
	@Autowired
	protected ObjectMapper objectMapper;

	/**
	 * Repositories are exposed to integration tests for fixture setup and
	 * post-request state verification.
	 */
	@Autowired
	protected UserRepository userRepository;

	/**
	 * Team repository used when tests need to reload mutated team aggregates.
	 */
	@Autowired
	protected TeamRepository teamRepository;

	/**
	 * Hackathon repository used for direct fixture setup and state assertions.
	 */
	@Autowired
	protected HackathonRepository hackathonRepository;

	/**
	 * Password encoder must match production security configuration so saved
	 * test users can authenticate through the real login endpoint.
	 */
	@Autowired
	protected PasswordEncoder passwordEncoder;

	/**
	 * Creates a collision-resistant email address for tests that register or
	 * persist users.
	 */
	protected @NonNull String uniqueEmail() {
		return "user+" + UUID.randomUUID() + "@example.com";
	}

	/**
	 * Persists a user with the shared password and the requested role.
	 *
	 * <p>{@code saveAndFlush} makes the user immediately visible to login and
	 * authorization code that runs later in the same test.</p>
	 */
	protected @NonNull User saveUser(Role role) {
		User user = new User();
		user.setEmail(uniqueEmail());
		user.setPasswordHash(passwordEncoder.encode(PASSWORD));
		user.setRole(role);
		return userRepository.saveAndFlush(user);
	}

	/**
	 * Logs in an existing test user and returns a JWT for authenticated
	 * requests.
	 */
	protected @NonNull String tokenFor(User user) throws Exception {
		User requiredUser = requiredUser(user, "User is required");
		MvcResult login = postJson("/api/auth/login", loginPayload(requiredUser.getEmail(), PASSWORD))
			.andExpect(status().isOk())
			.andReturn();
		return extractToken(login);
	}

	/**
	 * Reads a top-level numeric {@code id} field from a JSON response.
	 */
	protected @NonNull Long idFrom(MvcResult result) throws Exception {
		return objectMapper
			.readTree(requiredResult(result).getResponse().getContentAsString())
			.path("id")
			.asLong();
	}

	/**
	 * Reads a top-level numeric field from a JSON response, useful for response
	 * DTOs that expose nested entity IDs under names such as
	 * {@code teamId} or {@code hackathonId}.
	 */
	protected @NonNull Long nestedIdFrom(
		MvcResult result,
		String fieldName
	) throws Exception {
		return objectMapper
			.readTree(requiredResult(result).getResponse().getContentAsString())
			.path(requiredString(fieldName, "Field name is required"))
			.asLong();
	}

	/**
	 * Performs an unauthenticated GET request.
	 */
	protected @NonNull ResultActions get(String uri) throws Exception {
		return mockMvc.perform(MockMvcRequestBuilders.get(requiredString(uri, "URI is required")));
	}

	/**
	 * Performs an authenticated GET request using a Bearer token.
	 */
	protected @NonNull ResultActions getWithBearer(
		String uri,
		String token
	) throws Exception {
		return mockMvc.perform(
			MockMvcRequestBuilders
				.get(requiredString(uri, "URI is required"))
				.header(HttpHeaders.AUTHORIZATION, bearerToken(token))
		);
	}

	/**
	 * Performs an unauthenticated JSON POST request.
	 */
	protected @NonNull ResultActions postJson(
		String uri,
		Object body
	) throws Exception {
		return mockMvc.perform(
			MockMvcRequestBuilders
				.post(requiredString(uri, "URI is required"))
				.contentType(jsonMediaType())
				.content(writeJson(body))
		);
	}

	/**
	 * Performs an authenticated JSON POST request.
	 */
	protected @NonNull ResultActions postJsonWithBearer(
		String uri,
		String token,
		Object body
	) throws Exception {
		return mockMvc.perform(
			MockMvcRequestBuilders
				.post(requiredString(uri, "URI is required"))
				.header(HttpHeaders.AUTHORIZATION, bearerToken(token))
				.contentType(jsonMediaType())
				.content(writeJson(body))
		);
	}

	/**
	 * Performs an authenticated JSON PUT request.
	 */
	protected @NonNull ResultActions putJsonWithBearer(
		String uri,
		String token,
		Object body
	) throws Exception {
		return mockMvc.perform(
			MockMvcRequestBuilders
				.put(requiredString(uri, "URI is required"))
				.header(HttpHeaders.AUTHORIZATION, bearerToken(token))
				.contentType(jsonMediaType())
				.content(writeJson(body))
		);
	}

	/**
	 * Performs an authenticated JSON PATCH request.
	 */
	protected @NonNull ResultActions patchJsonWithBearer(
		String uri,
		String token,
		Object body
	) throws Exception {
		return mockMvc.perform(
			MockMvcRequestBuilders
				.patch(requiredString(uri, "URI is required"))
				.header(HttpHeaders.AUTHORIZATION, bearerToken(token))
				.contentType(jsonMediaType())
				.content(writeJson(body))
		);
	}

	/**
	 * Extracts the JWT returned by auth endpoints.
	 */
	protected @NonNull String extractToken(MvcResult result) throws Exception {
		JsonNode json = objectMapper.readTree(
			requiredResult(result).getResponse().getContentAsString()
		);
		return json.path("token").asText();
	}

	/**
	 * Builds a register request payload with the same shape as
	 * {@code RegisterRequest}.
	 */
	protected @NonNull Map<String, String> registerPayload(
		String email,
		String password
	) {
		return Map.of(
			"email",
			requiredString(email, "Email is required"),
			"password",
			requiredString(password, "Password is required")
		);
	}

	/**
	 * Builds a login request payload with the same shape as
	 * {@code LoginRequest}.
	 */
	protected @NonNull Map<String, String> loginPayload(
		String email,
		String password
	) {
		return Map.of(
			"email",
			requiredString(email, "Email is required"),
			"password",
			requiredString(password, "Password is required")
		);
	}




	

	/**
	 * Builds a team invitation payload.
	 */
	protected @NonNull Map<String, Object> createInvitationPayload(Long invitedUserId) {
		return Map.of(
			"invitedUserId",
			requiredLong(invitedUserId, "Invited user ID is required")
		);
	}

	/**
	 * Builds a hackathon team-registration payload.
	 */
	protected @NonNull Map<String, Object> registerTeamPayload(Long teamId) {
		return Map.of("teamId", requiredLong(teamId, "Team ID is required"));
	}

	/**
	 * Builds a valid submission payload reused by workflow and coverage tests.
	 */
	protected @NonNull Map<String, Object> submissionPayload() {
		return Map.of(
			"projectName", "AI Study Buddy",
			"repositoryUrl", "https://github.com/hackhub/ai-study-buddy",
			"demoUrl", "https://demo.hackhub.app/study-buddy",
			"description", "A focused study planning assistant"
		);
	}

	/**
	 * Builds a judge evaluation payload.
	 */
	protected @NonNull Map<String, Object> evaluationPayload(int score) {
		return Map.of("score", score, "comment", "Strong implementation");
	}

	/**
	 * Builds a winner declaration payload.
	 */
	protected @NonNull Map<String, Object> winnerPayload(Long teamId) {
		return Map.of("winnerTeamId", requiredLong(teamId, "Winner team ID is required"));
	}

	/**
	 * Adds a judge directly to a hackathon fixture when a test needs to bypass
	 * the organizer HTTP endpoint.
	 */
	protected void assignJudge(Hackathon hackathon, User judge) {
		Hackathon requiredHackathon = requiredHackathon(hackathon, "Hackathon is required");
		requiredHackathon.getJudges().add(requiredUser(judge, "Judge is required"));
		hackathonRepository.saveAndFlush(requiredHackathon);
	}

	/**
	 * Adds a mentor directly to a hackathon fixture when a test needs to bypass
	 * the organizer HTTP endpoint.
	 */
	protected void assignMentor(Hackathon hackathon, User mentor) {
		Hackathon requiredHackathon = requiredHackathon(hackathon, "Hackathon is required");
		requiredHackathon.getMentors().add(requiredUser(mentor, "Mentor is required"));
		hackathonRepository.saveAndFlush(requiredHackathon);
	}

	/**
	 * Reloads a team from the database after requests that mutate team state.
	 */
	protected @NonNull Team reloadTeam(Long teamId) {
		return teamRepository
			.findById(requiredLong(teamId, "Team ID is required"))
			.orElseThrow();
	}

	/**
	 * Reloads a hackathon from the database after requests that mutate lifecycle
	 * or assignment state.
	 */
	protected @NonNull Hackathon reloadHackathon(Long hackathonId) {
		return hackathonRepository
			.findById(requiredLong(hackathonId, "Hackathon ID is required"))
			.orElseThrow();
	}

	/**
	 * Keeps all helper-generated requests on the same JSON content type.
	 */
	@NonNull
	private MediaType jsonMediaType() {
		return MediaType.APPLICATION_JSON;
	}

	/**
	 * Serializes request payload maps and DTO-like objects using the same
	 * ObjectMapper configured by Spring Boot.
	 */
	@NonNull
	private String writeJson(Object body) throws Exception {
		return objectMapper.writeValueAsString(requiredObject(body, "Body is required"));
	}

	/**
	 * Centralizes the Authorization header format used by secured requests.
	 */
	@NonNull
	private String bearerToken(String token) {
		return "Bearer " + requiredString(token, "Token is required");
	}

	/**
	 * Private null guards keep helper failures close to the caller and avoid
	 * ambiguous NullPointerExceptions inside MockMvc or Jackson.
	 */
	@NonNull
	private String requiredString(String value, String message) {
		if (value == null) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	@NonNull
	private Long requiredLong(Long value, String message) {
		if (value == null) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	@NonNull
	private Object requiredObject(Object value, String message) {
		if (value == null) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	@NonNull
	private MvcResult requiredResult(MvcResult value) {
		if (value == null) {
			throw new IllegalArgumentException("MVC result is required");
		}
		return value;
	}

	@NonNull
	private User requiredUser(User value, String message) {
		if (value == null) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	@NonNull
	private Hackathon requiredHackathon(Hackathon value, String message) {
		if (value == null) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}
}
