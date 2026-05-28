package com.hackhub.application.service;

import com.hackhub.api.dto.request.LoginRequest;
import com.hackhub.api.dto.request.RegisterRequest;
import com.hackhub.api.dto.response.AuthResponse;
import com.hackhub.api.dto.response.UserResponse;
import com.hackhub.api.exception.ConflictException;
import com.hackhub.api.exception.NotFoundException;
import com.hackhub.application.mapper.UserMapper;
import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.UserRepository;
import com.hackhub.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final UserMapper userMapper;

	public AuthService(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		AuthenticationManager authenticationManager,
		JwtService jwtService,
		UserMapper userMapper
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.userMapper = userMapper;
	}

	public AuthResponse register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			throw new ConflictException("Email is already registered");
		}

		User user = new User();
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setRole(Role.USER);

		User savedUser = userRepository.save(user);
		String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getRole().name());
		return new AuthResponse(token, userMapper.toResponse(savedUser));
	}

	public AuthResponse login(LoginRequest request) {
		String email = request.email().trim().toLowerCase();
		authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(email, request.password())
		);

		User user = userRepository
			.findByEmail(email)
			.orElseThrow(() -> new NotFoundException("User not found"));

		String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
		return new AuthResponse(token, userMapper.toResponse(user));
	}

	public UserResponse currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			throw new NotFoundException("No authenticated user");
		}

		User user = userRepository
			.findByEmail(authentication.getName())
			.orElseThrow(() -> new NotFoundException("User not found"));
		return userMapper.toResponse(user);
	}
}
