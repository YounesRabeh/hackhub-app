package com.hackhub.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures Spring Security for the HackHub application.
 *
 * <p>This configuration defines authentication and authorization rules,
 * disables stateful login mechanisms, registers JWT-based authentication,
 * and exposes security-related beans used by the application.</p>
 *
 * <p>The application uses stateless authentication, meaning each protected
 * request must provide a valid JWT instead of relying on server-side sessions.</p>
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final Environment environment;

	/**
	 * Configures the HTTP security filter chain.
	 *
	 * <p>This method defines which endpoints are publicly accessible and which
	 * require authentication. Public endpoints include authentication routes,
	 * public hackathon listing routes, and development-only tools such as the
	 * H2 console and Swagger UI when the {@code dev} profile is active.</p>
	 *
	 * @param http the HTTP security configuration object
	 * @return the configured security filter chain
	 * @throws Exception if the security configuration cannot be built
	 */
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(handling -> handling.authenticationEntryPoint(new HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED)))
			.authorizeHttpRequests(authorize -> {
				authorize.requestMatchers("/error").permitAll();
				authorize.requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll();
				authorize.requestMatchers(HttpMethod.GET, "/api/hackathons", "/api/hackathons/*").permitAll();
				if (environment.acceptsProfiles(Profiles.of("dev"))) {
					authorize.requestMatchers("/h2-console/**").permitAll();
					authorize.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll();
				}
				authorize.anyRequest().authenticated();
			})
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * Provides the password encoder used to hash and verify user passwords.
	 *
	 * @return a BCrypt-based password encoder
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Exposes Spring Security's authentication manager.
	 *
	 * <p>The authentication manager is used by authentication services to
	 * validate user credentials during login.</p>
	 *
	 * @param configuration the Spring Security authentication configuration
	 * @return the configured authentication manager
	 * @throws Exception if the authentication manager cannot be obtained
	 */
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
		throws Exception {
		return configuration.getAuthenticationManager();
	}
}