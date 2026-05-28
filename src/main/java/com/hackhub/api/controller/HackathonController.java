package com.hackhub.api.controller;

import com.hackhub.api.dto.request.CreateHackathonRequest;
import com.hackhub.api.dto.request.DeclareWinnerRequest;
import com.hackhub.api.dto.request.RegisterTeamToHackathonRequest;
import com.hackhub.api.dto.request.UpdateHackathonStatusRequest;
import com.hackhub.api.dto.request.UpsertSubmissionRequest;
import com.hackhub.api.dto.response.HackathonRegistrationResponse;
import com.hackhub.api.dto.response.HackathonResponse;
import com.hackhub.api.dto.response.SubmissionResponse;
import com.hackhub.application.service.HackathonRegistrationService;
import com.hackhub.application.service.HackathonService;
import com.hackhub.application.service.OrganizerService;
import com.hackhub.application.service.SubmissionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hackathons")
@RequiredArgsConstructor
public class HackathonController {

	private final HackathonService hackathonService;
	private final HackathonRegistrationService hackathonRegistrationService;
	private final SubmissionService submissionService;
	private final OrganizerService organizerService;

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

	@PostMapping("/{hackathonId}/judges/{judgeId}")
	public HackathonResponse addJudge(
		@PathVariable Long hackathonId,
		@PathVariable Long judgeId
	) {
		return hackathonService.addJudge(hackathonId, judgeId);
	}

	@PatchMapping("/{hackathonId}/status")
	public HackathonResponse updateStatus(
		@PathVariable Long hackathonId,
		@Valid @RequestBody UpdateHackathonStatusRequest request
	) {
		return hackathonService.updateStatus(hackathonId, request);
	}

	@PostMapping("/{hackathonId}/registrations")
	@ResponseStatus(HttpStatus.CREATED)
	public HackathonRegistrationResponse registerTeam(
		@PathVariable Long hackathonId,
		@Valid @RequestBody RegisterTeamToHackathonRequest request
	) {
		return hackathonRegistrationService.registerTeam(hackathonId, request);
	}

	@GetMapping("/{hackathonId}/registrations")
	public List<HackathonRegistrationResponse> listRegistrations(@PathVariable Long hackathonId) {
		return hackathonRegistrationService.listRegistrations(hackathonId);
	}

	@PutMapping("/{hackathonId}/submissions/my-team")
	public SubmissionResponse upsertMyTeamSubmission(
		@PathVariable Long hackathonId,
		@Valid @RequestBody UpsertSubmissionRequest request
	) {
		return submissionService.upsertMyTeamSubmission(hackathonId, request);
	}

	@GetMapping("/{hackathonId}/submissions/my-team")
	public SubmissionResponse getMyTeamSubmission(@PathVariable Long hackathonId) {
		return submissionService.getMyTeamSubmission(hackathonId);
	}

	@GetMapping("/{hackathonId}/submissions")
	public List<SubmissionResponse> listSubmissions(@PathVariable Long hackathonId) {
		return submissionService.listHackathonSubmissions(hackathonId);
	}

	@PostMapping("/{hackathonId}/winner")
	public HackathonResponse declareWinner(
		@PathVariable Long hackathonId,
		@Valid @RequestBody DeclareWinnerRequest request
	) {
		return organizerService.declareWinner(hackathonId, request);
	}
}
