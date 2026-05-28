package com.hackhub.api.controller;

import com.hackhub.support.IntegrationTestSupport;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthSecurityIntegrationTest extends IntegrationTestSupport {

	private static final String STRONG_PASSWORD = "StrongPass123!";

	@Test
	void registerReturnsTokenAndNormalizedUser() throws Exception {
		String emailInput = "User+" + System.nanoTime() + "@Example.COM";
		String normalizedEmail = emailInput.toLowerCase(Locale.ROOT);

		postJson("/api/auth/register", registerPayload(emailInput, STRONG_PASSWORD))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.token").isString())
			.andExpect(jsonPath("$.user.email").value(normalizedEmail))
			.andExpect(jsonPath("$.user.role").value("USER"));
	}

	@Test
	void registerDuplicateEmailReturnsConflict() throws Exception {
		String email = uniqueEmail();

		postJson("/api/auth/register", registerPayload(email, STRONG_PASSWORD))
			.andExpect(status().isCreated());

		postJson("/api/auth/register", registerPayload(email.toUpperCase(Locale.ROOT), STRONG_PASSWORD))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status").value(409))
			.andExpect(jsonPath("$.message").value("Email is already registered"))
			.andExpect(jsonPath("$.path").value("/api/auth/register"));
	}

	@Test
	void loginReturnsTokenForRegisteredUser() throws Exception {
		String email = uniqueEmail();
		postJson("/api/auth/register", registerPayload(email, STRONG_PASSWORD))
			.andExpect(status().isCreated());

		postJson("/api/auth/login", loginPayload(email, STRONG_PASSWORD))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").isString())
			.andExpect(jsonPath("$.user.email").value(email))
			.andExpect(jsonPath("$.user.role").value("USER"));
	}

	@Test
	void meEndpointRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void meEndpointReturnsCurrentUserWhenAuthenticated() throws Exception {
		String email = uniqueEmail();
		MvcResult registration = postJson("/api/auth/register", registerPayload(email, STRONG_PASSWORD))
			.andExpect(status().isCreated())
			.andReturn();
		String token = extractToken(registration);

		getWithBearer("/api/auth/me", token)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value(email))
			.andExpect(jsonPath("$.role").value("USER"));
	}

	@Test
	void unknownProtectedRouteWithoutTokenIsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/teams/me"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void unknownPublicHackathonRouteStaysPublicAndReturnsNotFound() throws Exception {
		mockMvc.perform(get("/api/hackathons/not-implemented"))
			.andExpect(status().isNotFound());
	}
}
