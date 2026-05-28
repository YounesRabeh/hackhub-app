package com.hackhub.security;

import com.hackhub.api.exception.NotFoundException;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

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
