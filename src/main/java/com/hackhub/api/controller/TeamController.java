package com.hackhub.api.controller;

import com.hackhub.api.dto.request.CreateTeamRequest;
import com.hackhub.api.dto.response.TeamResponse;
import com.hackhub.application.service.TeamService;
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
public class TeamController {

	private final TeamService teamService;

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
