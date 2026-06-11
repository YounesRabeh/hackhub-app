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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(EndpointCoverageTestConfiguration.class)
@SuppressWarnings("null")
public abstract class IntegrationTestSupport {

	protected static final String PASSWORD = "Password123!";

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected UserRepository userRepository;

	@Autowired
	protected TeamRepository teamRepository;

	@Autowired
	protected HackathonRepository hackathonRepository;

	@Autowired
	protected PasswordEncoder passwordEncoder;

	protected @NonNull String uniqueEmail() {
		return "user+" + UUID.randomUUID() + "@example.com";
	}

	protected @NonNull User saveUser(Role role) {
		User user = new User();
		user.setEmail(uniqueEmail());
		user.setPasswordHash(passwordEncoder.encode(PASSWORD));
		user.setRole(role);
		return userRepository.saveAndFlush(user);
	}

	protected @NonNull String tokenFor(User user) throws Exception {
		User requiredUser = requiredUser(user, "User is required");
		MvcResult login = postJson("/api/auth/login", loginPayload(requiredUser.getEmail(), PASSWORD))
			.andExpect(status().isOk())
			.andReturn();
		return extractToken(login);
	}

	protected @NonNull Long idFrom(MvcResult result) throws Exception {
		return objectMapper
			.readTree(requiredResult(result).getResponse().getContentAsString())
			.path("id")
			.asLong();
	}

	protected @NonNull Long nestedIdFrom(
		MvcResult result,
		String fieldName
	) throws Exception {
		return objectMapper
			.readTree(requiredResult(result).getResponse().getContentAsString())
			.path(requiredString(fieldName, "Field name is required"))
			.asLong();
	}

	protected @NonNull ResultActions get(String uri) throws Exception {
		return mockMvc.perform(MockMvcRequestBuilders.get(requiredString(uri, "URI is required")));
	}

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

	protected @NonNull String extractToken(MvcResult result) throws Exception {
		JsonNode json = objectMapper.readTree(
			requiredResult(result).getResponse().getContentAsString()
		);
		return json.path("token").asText();
	}

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

	protected @NonNull Map<String, Object> createTeamPayload(String name) {
		return Map.of("name", requiredString(name, "Team name is required"));
	}

	protected @NonNull Map<String, Object> createInvitationPayload(Long invitedUserId) {
		return Map.of(
			"invitedUserId",
			requiredLong(invitedUserId, "Invited user ID is required")
		);
	}

	protected @NonNull Map<String, Object> registerTeamPayload(Long teamId) {
		return Map.of("teamId", requiredLong(teamId, "Team ID is required"));
	}

	protected @NonNull Map<String, Object> submissionPayload() {
		return Map.of(
			"projectName", "AI Study Buddy",
			"repositoryUrl", "https://github.com/hackhub/ai-study-buddy",
			"demoUrl", "https://demo.hackhub.app/study-buddy",
			"description", "A focused study planning assistant"
		);
	}

	protected @NonNull Map<String, Object> evaluationPayload(int score) {
		return Map.of("score", score, "comment", "Strong implementation");
	}

	protected @NonNull Map<String, Object> winnerPayload(Long teamId) {
		return Map.of("winnerTeamId", requiredLong(teamId, "Winner team ID is required"));
	}

	protected void assignJudge(Hackathon hackathon, User judge) {
		Hackathon requiredHackathon = requiredHackathon(hackathon, "Hackathon is required");
		requiredHackathon.getJudges().add(requiredUser(judge, "Judge is required"));
		hackathonRepository.saveAndFlush(requiredHackathon);
	}

	protected void assignMentor(Hackathon hackathon, User mentor) {
		Hackathon requiredHackathon = requiredHackathon(hackathon, "Hackathon is required");
		requiredHackathon.getMentors().add(requiredUser(mentor, "Mentor is required"));
		hackathonRepository.saveAndFlush(requiredHackathon);
	}

	protected @NonNull Team reloadTeam(Long teamId) {
		return teamRepository
			.findById(requiredLong(teamId, "Team ID is required"))
			.orElseThrow();
	}

	protected @NonNull Hackathon reloadHackathon(Long hackathonId) {
		return hackathonRepository
			.findById(requiredLong(hackathonId, "Hackathon ID is required"))
			.orElseThrow();
	}

	@NonNull
	private MediaType jsonMediaType() {
		return MediaType.APPLICATION_JSON;
	}

	@NonNull
	private String writeJson(Object body) throws Exception {
		return objectMapper.writeValueAsString(requiredObject(body, "Body is required"));
	}

	@NonNull
	private String bearerToken(String token) {
		return "Bearer " + requiredString(token, "Token is required");
	}

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
