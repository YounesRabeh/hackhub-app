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

	protected String uniqueEmail() {
		return "user+" + UUID.randomUUID() + "@example.com";
	}

	protected User saveUser(Role role) {
		User user = new User();
		user.setEmail(uniqueEmail());
		user.setPasswordHash(passwordEncoder.encode(PASSWORD));
		user.setRole(role);
		return userRepository.saveAndFlush(user);
	}

	protected String tokenFor(User user) throws Exception {
		MvcResult login = postJson("/api/auth/login", loginPayload(user.getEmail(), PASSWORD))
			.andExpect(status().isOk())
			.andReturn();
		return extractToken(login);
	}

	protected Long idFrom(MvcResult result) throws Exception {
		return objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asLong();
	}

	protected Long nestedIdFrom(MvcResult result, String fieldName) throws Exception {
		return objectMapper.readTree(result.getResponse().getContentAsString()).path(fieldName).asLong();
	}

	protected ResultActions get(String uri) throws Exception {
		return mockMvc.perform(MockMvcRequestBuilders.get(uri));
	}

	protected ResultActions getWithBearer(String uri, String token) throws Exception {
		return mockMvc.perform(
			MockMvcRequestBuilders
				.get(uri)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
		);
	}

	protected ResultActions postJson(String uri, Object body) throws Exception {
		return mockMvc.perform(
			MockMvcRequestBuilders
				.post(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
		);
	}

	protected ResultActions postJsonWithBearer(String uri, String token, Object body) throws Exception {
		return mockMvc.perform(
			MockMvcRequestBuilders
				.post(uri)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
		);
	}

	protected ResultActions putJsonWithBearer(String uri, String token, Object body) throws Exception {
		return mockMvc.perform(
			MockMvcRequestBuilders
				.put(uri)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
		);
	}

	protected ResultActions patchJsonWithBearer(String uri, String token, Object body) throws Exception {
		return mockMvc.perform(
			MockMvcRequestBuilders
				.patch(uri)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
		);
	}

	protected String extractToken(MvcResult result) throws Exception {
		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		return json.path("token").asText();
	}

	protected Map<String, String> registerPayload(String email, String password) {
		return Map.of("email", email, "password", password);
	}

	protected Map<String, String> loginPayload(String email, String password) {
		return Map.of("email", email, "password", password);
	}

	protected Map<String, Object> createTeamPayload(String name) {
		return Map.of("name", name);
	}

	protected Map<String, Object> createInvitationPayload(Long invitedUserId) {
		return Map.of("invitedUserId", invitedUserId);
	}

	protected Map<String, Object> registerTeamPayload(Long teamId) {
		return Map.of("teamId", teamId);
	}

	protected Map<String, Object> submissionPayload() {
		return Map.of(
			"projectName", "AI Study Buddy",
			"repositoryUrl", "https://github.com/hackhub/ai-study-buddy",
			"demoUrl", "https://demo.hackhub.app/study-buddy",
			"description", "A focused study planning assistant"
		);
	}

	protected Map<String, Object> evaluationPayload(int score) {
		return Map.of("score", score, "comment", "Strong implementation");
	}

	protected Map<String, Object> winnerPayload(Long teamId) {
		return Map.of("winnerTeamId", teamId);
	}

	protected void assignJudge(Hackathon hackathon, User judge) {
		hackathon.getJudges().add(judge);
		hackathonRepository.saveAndFlush(hackathon);
	}

	protected void assignMentor(Hackathon hackathon, User mentor) {
		hackathon.getMentors().add(mentor);
		hackathonRepository.saveAndFlush(hackathon);
	}

	protected Team reloadTeam(Long teamId) {
		return teamRepository.findById(teamId).orElseThrow();
	}

	protected Hackathon reloadHackathon(Long hackathonId) {
		return hackathonRepository.findById(hackathonId).orElseThrow();
	}
}
