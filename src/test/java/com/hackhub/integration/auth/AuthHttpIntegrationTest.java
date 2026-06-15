package com.hackhub.integration.auth;

import com.hackhub.testsupport.IntegrationTestSupport;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthHttpIntegrationTest extends IntegrationTestSupport {

	@Value("${app.security.jwt.secret}")
	private String jwtSecret;

	@Test
	void registerAndLoginReturnJwtTokens() throws Exception {
		String emailInput = "User+" + System.nanoTime() + "@Example.COM";
		String normalizedEmail = emailInput.toLowerCase(Locale.ROOT);

		postJson("/api/auth/register", registerPayload(emailInput, PASSWORD))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.token").isString())
			.andExpect(jsonPath("$.user.email").value(normalizedEmail))
			.andExpect(jsonPath("$.user.role").value("USER"))
			.andExpect(jsonPath("$.user.password").doesNotExist())
			.andExpect(jsonPath("$.user.passwordHash").doesNotExist());

		postJson("/api/auth/login", loginPayload(normalizedEmail, PASSWORD))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").isString())
			.andExpect(jsonPath("$.user.email").value(normalizedEmail))
			.andExpect(jsonPath("$.user.password").doesNotExist())
			.andExpect(jsonPath("$.user.passwordHash").doesNotExist());
	}

	@Test
	void protectedEndpointRejectsMissingToken() throws Exception {
		get("/api/auth/me")
			.andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpointRejectsMalformedToken() throws Exception {
		getWithBearer("/api/auth/me", "not-a-jwt")
			.andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpointRejectsInvalidSignatureToken() throws Exception {
		getWithBearer("/api/auth/me", tokenSignedWithDifferentSecret(uniqueEmail()))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpointRejectsExpiredToken() throws Exception {
		getWithBearer("/api/auth/me", expiredToken(uniqueEmail()))
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
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.email").value(email))
			.andExpect(jsonPath("$.role").value("USER"));
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
	void registerRejectsEmailWithSurroundingWhitespace() throws Exception {
		String emailInput = "  Trimmed+" + System.nanoTime() + "@Example.COM  ";

		postJson("/api/auth/register", registerPayload(emailInput, PASSWORD))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Validation failed"))
			.andExpect(jsonPath("$.details", validationDetailContaining("email")));
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
	void loginRejectsUnknownEmail() throws Exception {
		postJson("/api/auth/login", loginPayload(uniqueEmail(), PASSWORD))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").value("Authentication failed"));
	}

	@Test
	void registerRejectsInvalidPayload() throws Exception {
		postJson("/api/auth/register", registerPayload("not-an-email", "short"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Validation failed"))
			.andExpect(jsonPath("$.details", validationDetailContaining("email")))
			.andExpect(jsonPath("$.details", validationDetailContaining("password")));
	}

	@Test
	void registerRejectsBlankFields() throws Exception {
		postJson("/api/auth/register", registerPayload("", ""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Validation failed"))
			.andExpect(jsonPath("$.details", validationDetailContaining("email")))
			.andExpect(jsonPath("$.details", validationDetailContaining("password")));
	}

	@Test
	void registerRejectsMalformedJson() throws Exception {
		mockMvc.perform(
			MockMvcRequestBuilders
				.post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Malformed JSON request"))
			.andExpect(jsonPath("$.path").value("/api/auth/register"));
	}

	@Test
	void loginRejectsInvalidPayload() throws Exception {
		postJson("/api/auth/login", loginPayload("not-an-email", ""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Validation failed"))
			.andExpect(jsonPath("$.details", validationDetailContaining("email")))
			.andExpect(jsonPath("$.details", validationDetailContaining("password")));
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

	@Test
	void loginRejectsEmailWithSurroundingWhitespace() throws Exception {
		String email = uniqueEmail();
		postJson("/api/auth/register", registerPayload(email, PASSWORD))
			.andExpect(status().isCreated());

		postJson("/api/auth/login", loginPayload("  " + email.toUpperCase(Locale.ROOT) + "  ", PASSWORD))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Validation failed"))
			.andExpect(jsonPath("$.details", validationDetailContaining("email")));
	}

	@SuppressWarnings("null")
	private static @NonNull Matcher<? super Iterable<? super String>> validationDetailContaining(
		@NonNull String detail
	) {
		return hasItem(containsString(detail));
	}

	private String tokenSignedWithDifferentSecret(String subject) {
		return Jwts.builder()
			.subject(subject)
			.issuedAt(Date.from(Instant.now()))
			.expiration(Date.from(Instant.now().plusSeconds(3600)))
			.signWith(Keys.hmacShaKeyFor(
				"invalid-signing-key-for-auth-tests!!".getBytes(StandardCharsets.UTF_8)
			))
			.compact();
	}

	private String expiredToken(String subject) {
		Instant now = Instant.now();
		return Jwts.builder()
			.subject(subject)
			.claim("role", "USER")
			.issuedAt(Date.from(now.minusSeconds(7200)))
			.expiration(Date.from(now.minusSeconds(3600)))
			.signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)))
			.compact();
	}
}
