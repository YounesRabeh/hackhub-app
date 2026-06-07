package com.hackhub.api.controller;

import com.hackhub.api.OpenApiConfig;
import com.hackhub.api.dto.request.CreateTeamRequest;
import com.hackhub.api.dto.response.TeamResponse;
import com.hackhub.application.service.TeamService;
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
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(
	name = "Teams",
	description = "Create teams and retrieve team information for the authenticated user."
)
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TeamController {

	private final TeamService teamService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
		summary = "Create team",
		description = "Use case: a participant creates a new team before inviting members or registering for a hackathon."
	)
	public TeamResponse createTeam(@Valid @RequestBody CreateTeamRequest request) {
		return teamService.createTeam(request);
	}

	@GetMapping("/me")
	@Operation(
		summary = "Get my team",
		description = "Use case: the frontend fetches the team currently associated with the authenticated user."
	)
	public TeamResponse currentUserTeam() {
		return teamService.currentUserTeam();
	}
}
