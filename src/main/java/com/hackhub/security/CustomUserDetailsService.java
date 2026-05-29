package com.hackhub.security;

import com.hackhub.api.exception.NotFoundException;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService}.
 *
 * <p>This service is responsible for loading user information during the
 * authentication process. User credentials and roles are retrieved from the
 * application's persistence layer and converted into a Spring Security
 * {@link UserDetails} instance.</p>
 *
 * <p>Users are identified by their email address, which acts as the username
 * within the authentication system.</p>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	/**
	 * Loads a user by email address and converts it into a Spring Security
	 * {@link UserDetails} object.
	 *
	 * @param username the email address used for authentication
	 * @return the corresponding {@link UserDetails} instance
	 * @throws NotFoundException if no user exists with the specified email
	 */
	@Override
	public UserDetails loadUserByUsername(String username) {
		User user = userRepository
			.findByEmail(username)
			.orElseThrow(() -> new NotFoundException("User not found"));

		return org.springframework.security.core.userdetails.User
			.withUsername(user.getEmail())
			.password(user.getPasswordHash())
			.roles(user.getRole().name())
			.build();
	}
}