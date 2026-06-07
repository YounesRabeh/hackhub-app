package com.hackhub.api.controller;

import com.hackhub.api.OpenApiConfig;
import com.hackhub.api.dto.request.LoginRequest;
import com.hackhub.api.dto.request.RegisterRequest;
import com.hackhub.api.dto.response.AuthResponse;
import com.hackhub.api.dto.response.UserResponse;
import com.hackhub.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
	name = "Authentication",
	description = "Register, authenticate, and fetch the currently authenticated user."
)
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
		summary = "Register a new user",
		description = "Use case: a new participant creates an account and immediately receives authentication data."
	)
	public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	@PostMapping("/login")
	@Operation(
		summary = "Authenticate user",
		description = "Use case: an existing user signs in with email and password to receive a valid token."
	)
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@GetMapping("/me")
	@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
	@Operation(
		summary = "Get current user profile",
		description = "Use case: the frontend loads the authenticated user profile for session personalization and authorization checks."
	)
	public UserResponse me() {
		return authService.currentUser();
	}
}
