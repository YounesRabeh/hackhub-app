package com.hackhub.testsupport;

import com.hackhub.domain.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility methods for configuring Spring Security authentication in tests.
 */
public final class TestSecurity {

    private TestSecurity() {
    }

    /**
     * Authenticates the current test security context as the given user.
     *
     * @param user user to authenticate as
     */
    public static void authenticateAs(User user) {
        SecurityContextHolder
            .getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), null));
    }

    /**
     * Clears the current test security context.
     */
    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}