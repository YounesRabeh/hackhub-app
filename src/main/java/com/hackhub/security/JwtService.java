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

@Service
public class JwtService {

	private final String secret;
	private final long expirationMs;

	public JwtService(
		@Value("${app.security.jwt.secret}") String secret,
		@Value("${app.security.jwt.expiration-ms}") long expirationMs
	) {
		this.secret = secret;
		this.expirationMs = expirationMs;
	}

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

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		Date expiration = extractAllClaims(token).getExpiration();
		return username.equals(userDetails.getUsername()) && expiration.after(new Date());
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
			.verifyWith(getSigningKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	private SecretKey getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
