package com.hackhub.api.controller;

import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.UserRepository;
import com.hackhub.support.IntegrationTestSupport;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HackathonCoreIntegrationTest extends IntegrationTestSupport {

	private static final String PASSWORD = "StrongPass123!";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void publicHackathonListingWorksWithoutAuthentication() throws Exception {
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/hackathons"))
			.andExpect(status().isOk());
	}

	@Test
	void createHackathonRequiresOrganizerRole() throws Exception {
		String userEmail = uniqueEmail();
		postJson("/api/auth/register", registerPayload(userEmail, PASSWORD))
			.andExpect(status().isCreated());
		MvcResult loginResult = postJson("/api/auth/login", loginPayload(userEmail, PASSWORD))
			.andExpect(status().isOk())
			.andReturn();
		String token = extractToken(loginResult);

		postJsonWithBearer("/api/hackathons", token, validHackathonPayload())
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.message").value("Only organizers can create hackathons"));
	}

	@Test
	void organizerCanCreateHackathon() throws Exception {
		String organizerToken = tokenForRole(Role.ORGANIZER);

		postJsonWithBearer("/api/hackathons", organizerToken, validHackathonPayload())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.status").value("REGISTRATION_OPEN"));
	}

	@Test
	void addMentorRejectsNonMentorUsers() throws Exception {
		String organizerToken = tokenForRole(Role.ORGANIZER);
		Long hackathonId = createHackathon(organizerToken);
		User notMentor = userWithRole(Role.USER);

		postJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/mentors/" + notMentor.getId(),
			organizerToken,
			Map.of()
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Assigned user must have role MENTOR"));
	}

	@Test
	void statusTransitionRejectsInvalidMoves() throws Exception {
		String organizerToken = tokenForRole(Role.ORGANIZER);
		Long hackathonId = createHackathon(organizerToken);

		patchJsonWithBearer(
			"/api/hackathons/" + hackathonId + "/status",
			organizerToken,
			Map.of("status", "FINISHED")
		)
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath("$.message").value(
					"Invalid status transition from REGISTRATION_OPEN to FINISHED"
				)
			);
	}

	private Map<String, Object> validHackathonPayload() {
		LocalDateTime now = LocalDateTime.now().plusDays(2);
		return Map.of(
			"title",
			"Spring Hack",
			"description",
			"Build cool things",
			"registrationDeadline",
			now.plusDays(1),
			"submissionDeadline",
			now.plusDays(5),
			"startAt",
			now.plusDays(2),
			"endAt",
			now.plusDays(7),
			"prizeAmount",
			"5000.00"
		);
	}

	private Long createHackathon(String organizerToken) throws Exception {
		MvcResult result = postJsonWithBearer("/api/hackathons", organizerToken, validHackathonPayload())
			.andExpect(status().isCreated())
			.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asLong();
	}

	private User userWithRole(Role role) {
		User user = new User();
		user.setEmail(uniqueEmail());
		user.setPasswordHash(passwordEncoder.encode(PASSWORD));
		user.setRole(role);
		return userRepository.save(user);
	}

	private String tokenForRole(Role role) throws Exception {
		User user = userWithRole(role);
		MvcResult loginResult = postJson(
			"/api/auth/login",
			loginPayload(user.getEmail(), PASSWORD)
		)
			.andExpect(status().isOk())
			.andReturn();
		return extractToken(loginResult);
	}
}
