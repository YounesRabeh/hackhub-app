package com.hackhub.integration.auth;

import com.hackhub.testsupport.IntegrationTestSupport;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthHttpIntegrationTest extends IntegrationTestSupport {

	@Test
	void registerAndLoginReturnJwtTokens() throws Exception {
		String emailInput = "User+" + System.nanoTime() + "@Example.COM";
		String normalizedEmail = emailInput.toLowerCase(Locale.ROOT);

		postJson("/api/auth/register", registerPayload(emailInput, PASSWORD))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.token").isString())
			.andExpect(jsonPath("$.user.email").value(normalizedEmail))
			.andExpect(jsonPath("$.user.role").value("USER"));

		postJson("/api/auth/login", loginPayload(normalizedEmail, PASSWORD))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").isString())
			.andExpect(jsonPath("$.user.email").value(normalizedEmail));
	}

	@Test
	void protectedEndpointRejectsMissingToken() throws Exception {
		get("/api/auth/me")
			.andExpect(status().isUnauthorized());
	}

	@Test
	void currentUserEndpointReturnsAuthenticatedUser() throws Exception {
		String email = uniqueEmail();
		MvcResult registration = postJson("/api/auth/register", registerPayload(email, PASSWORD))
			.andExpect(status().isCreated())
			.andReturn();

		getWithBearer("/api/auth/me", extractToken(registration))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value(email));
	}

	@Test
	void registerRejectsDuplicateEmailEvenWithDifferentCase() throws Exception {
		String email = "Dup+" + System.nanoTime() + "@Example.com";

		postJson("/api/auth/register", registerPayload(email, PASSWORD))
			.andExpect(status().isCreated());

		postJson("/api/auth/register", registerPayload(email.toLowerCase(Locale.ROOT), PASSWORD))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value("Email is already registered"));
	}

	@Test
	void loginRejectsWrongPassword() throws Exception {
		String email = uniqueEmail();
		postJson("/api/auth/register", registerPayload(email, PASSWORD))
			.andExpect(status().isCreated());

		postJson("/api/auth/login", loginPayload(email, "WrongPassword123!"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").value("Authentication failed"));
	}

	@Test
	void registerRejectsInvalidPayload() throws Exception {
		postJson("/api/auth/register", registerPayload("not-an-email", "short"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Validation failed"))
			.andExpect(jsonPath("$.details", hasItem(containsString("email"))))
			.andExpect(jsonPath("$.details", hasItem(containsString("password"))));
	}

	@Test
	void loginAcceptsEmailWithDifferentCase() throws Exception {
		String email = "Case+" + System.nanoTime() + "@Example.com";
		postJson("/api/auth/register", registerPayload(email, PASSWORD))
			.andExpect(status().isCreated());

		postJson("/api/auth/login", loginPayload(email.toUpperCase(Locale.ROOT), PASSWORD))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.user.email").value(email.toLowerCase(Locale.ROOT)))
			.andExpect(jsonPath("$.token").isString());
	}
}
