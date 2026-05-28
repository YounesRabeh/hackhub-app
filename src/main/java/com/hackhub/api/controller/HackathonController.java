package com.hackhub.api.controller;

import com.hackhub.api.dto.request.CreateHackathonRequest;
import com.hackhub.api.dto.request.UpdateHackathonStatusRequest;
import com.hackhub.api.dto.response.HackathonResponse;
import com.hackhub.application.service.HackathonService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hackathons")
public class HackathonController {

	private final HackathonService hackathonService;

	public HackathonController(HackathonService hackathonService) {
		this.hackathonService = hackathonService;
	}

	@GetMapping
	public List<HackathonResponse> listHackathons() {
		return hackathonService.listHackathons();
	}

	@GetMapping("/{hackathonId}")
	public HackathonResponse getHackathon(@PathVariable Long hackathonId) {
		return hackathonService.getHackathon(hackathonId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public HackathonResponse createHackathon(@Valid @RequestBody CreateHackathonRequest request) {
		return hackathonService.createHackathon(request);
	}

	@PostMapping("/{hackathonId}/mentors/{mentorId}")
	public HackathonResponse addMentor(
		@PathVariable Long hackathonId,
		@PathVariable Long mentorId
	) {
		return hackathonService.addMentor(hackathonId, mentorId);
	}

	@PatchMapping("/{hackathonId}/status")
	public HackathonResponse updateStatus(
		@PathVariable Long hackathonId,
		@Valid @RequestBody UpdateHackathonStatusRequest request
	) {
		return hackathonService.updateStatus(hackathonId, request);
	}
}
