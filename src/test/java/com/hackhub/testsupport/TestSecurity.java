package com.hackhub.testsupport;

import com.hackhub.domain.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Test utility for installing and removing an authenticated principal in
 * Spring Security's current {@link org.springframework.security.core.context.SecurityContext}.
 *
 * <p>The application identifies the current user by the authentication name,
 * so {@link #authenticateAs(User)} uses the supplied user's email address as
 * the principal. No password or authorities are required because tests using
 * this helper exercise application services directly rather than performing
 * credential authentication through the security filter chain.</p>
 *
 * <p>Authentication is stored in Spring Security's thread-local context.
 * Tests should call {@link #clear()} during cleanup to prevent authentication
 * from leaking into another test executed on the same thread.</p>
 */
public final class TestSecurity {

    private TestSecurity() {
    }

    /**
     * Replaces the authentication in the current security context with a
     * token whose principal is {@code user.getEmail()}.
     *
     * <p>The token contains neither credentials nor granted authorities. It is
     * intended for service-level tests whose production code resolves the
     * caller from {@link org.springframework.security.core.Authentication#getName()}.</p>
     *
     * @param user application user whose email becomes the authenticated name;
     *             must not be {@code null}
     * @throws NullPointerException if {@code user} is {@code null}
     */
    public static void authenticateAs(User user) {
        SecurityContextHolder
            .getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), null));
    }

    /**
     * Removes all authentication data associated with the current thread.
     * This method is suitable for use from an {@code @AfterEach} cleanup method.
     */
    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
