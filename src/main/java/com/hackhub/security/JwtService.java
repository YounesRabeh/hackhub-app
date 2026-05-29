package com.hackhub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service responsible for generating, validating, and parsing JSON Web Tokens
 * (JWT) used for authentication and authorization.
 *
 * <p>This service creates signed JWTs containing user identity information and
 * role claims. It also provides utilities for extracting token data and
 * validating token authenticity and expiration.</p>
 *
 * <p>The signing secret and token expiration time are loaded from the
 * application configuration.</p>
 */
@Service
public class JwtService {

	/**
	 * Base64-encoded secret key used to sign and verify JWTs.
	 */
	private final String secret;

	/**
	 * Token validity duration expressed in milliseconds.
	 */
	private final long expirationMs;

	/**
	 * Creates a new JWT service using the configured signing secret and
	 * expiration period.
	 *
	 * @param secret the Base64-encoded signing secret
	 * @param expirationMs the token expiration time in milliseconds
	 */
	public JwtService(
		@Value("${app.security.jwt.secret}") String secret,
		@Value("${app.security.jwt.expiration-ms}") long expirationMs
	) {
		this.secret = secret;
		this.expirationMs = expirationMs;
	}

	/**
	 * Generates a signed JWT for the specified user.
	 *
	 * <p>The generated token contains the user's identifier as the subject,
	 * their role as a custom claim, and standard issued-at and expiration
	 * timestamps.</p>
	 *
	 * @param subject the user identifier, typically the user's email
	 * @param role the role associated with the user
	 * @return a signed JWT string
	 */
	public String generateToken(String subject, String role) {
		Instant now = Instant.now();
		Instant expiry = now.plusMillis(expirationMs);

		return Jwts.builder()
			.subject(subject)
			.claim("role", role)
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiry))
			.signWith(getSigningKey())
			.compact();
	}

	/**
	 * Extracts the username stored in the token subject.
	 *
	 * @param token the JWT to inspect
	 * @return the username contained in the token
	 */
	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	/**
	 * Validates a JWT against the specified user details.
	 *
	 * <p>A token is considered valid if:</p>
	 * <ul>
	 *   <li>The username contained in the token matches the user.</li>
	 *   <li>The token has not expired.</li>
	 * </ul>
	 *
	 * @param token the JWT to validate
	 * @param userDetails the expected authenticated user
	 * @return {@code true} if the token is valid, {@code false} otherwise
	 */
	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		Date expiration = extractAllClaims(token).getExpiration();
		return username.equals(userDetails.getUsername()) && expiration.after(new Date());
	}

	/**
	 * Extracts all claims contained in a signed JWT.
	 *
	 * @param token the JWT to parse
	 * @return the claims contained in the token
	 */
	private Claims extractAllClaims(String token) {
		return Jwts.parser()
			.verifyWith(getSigningKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	/**
	 * Creates the cryptographic signing key used for token signing and
	 * verification.
	 *
	 * @return the JWT signing key
	 */
	private SecretKey getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
