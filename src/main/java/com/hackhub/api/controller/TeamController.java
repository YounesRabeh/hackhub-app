package com.hackhub.api.controller;

import com.hackhub.api.dto.request.CreateTeamRequest;
import com.hackhub.api.dto.response.TeamResponse;
import com.hackhub.application.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

	private final TeamService teamService;

	public TeamController(TeamService teamService) {
		this.teamService = teamService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TeamResponse createTeam(@Valid @RequestBody CreateTeamRequest request) {
		return teamService.createTeam(request);
	}

	@GetMapping("/me")
	public TeamResponse currentUserTeam() {
		return teamService.currentUserTeam();
	}
}
