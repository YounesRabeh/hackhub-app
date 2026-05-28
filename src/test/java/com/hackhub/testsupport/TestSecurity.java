package com.hackhub.testsupport;

import com.hackhub.domain.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public final class TestSecurity {

	private TestSecurity() {
	}

	public static void authenticateAs(User user) {
		SecurityContextHolder
			.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), null));
	}

	public static void clear() {
		SecurityContextHolder.clearContext();
	}
}
