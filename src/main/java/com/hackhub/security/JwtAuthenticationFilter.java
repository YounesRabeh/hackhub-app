package com.hackhub.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.JwtException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

/**
 * Spring Security filter responsible for authenticating requests using
 * JSON Web Tokens (JWT).
 *
 * <p>This filter intercepts every incoming HTTP request, extracts the JWT
 * from the {@code Authorization} header, validates it, and establishes the
 * authenticated user within the Spring Security context.</p>
 *
 * <p>If the token is valid, a corresponding
 * {@link UsernamePasswordAuthenticationToken} is created and stored in the
 * {@link SecurityContextHolder}. If the token is missing or invalid, the
 * request continues without authentication.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final CustomUserDetailsService userDetailsService;

	/**
	 * Processes the incoming request and attempts JWT-based authentication.
	 *
	 * <p>The filter performs the following steps:</p>
	 * <ol>
	 *   <li>Extracts the {@code Authorization} header.</li>
	 *   <li>Verifies that the header contains a Bearer token.</li>
	 *   <li>Extracts the username from the JWT.</li>
	 *   <li>Loads the corresponding user details.</li>
	 *   <li>Validates the token.</li>
	 *   <li>Populates the Spring Security context if authentication succeeds.</li>
	 * </ol>
	 *
	 * <p>If authentication cannot be established, the request is forwarded
	 * unchanged through the filter chain.</p>
	 *
	 * @param request the incoming HTTP request
	 * @param response the outgoing HTTP response
	 * @param filterChain the filter chain used to continue request processing
	 * @throws ServletException if a servlet-related error occurs
	 * @throws IOException if an I/O error occurs while processing the request
	 */
	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(7);

		try {
			String username = jwtService.extractUsername(token);

			if (
				username != null &&
				SecurityContextHolder.getContext().getAuthentication() == null
			) {
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				if (jwtService.isTokenValid(token, userDetails)) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
						userDetails,
						null,
						userDetails.getAuthorities()
					);
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
		} catch (JwtException | IllegalArgumentException | AuthenticationException ex) {
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}
}
