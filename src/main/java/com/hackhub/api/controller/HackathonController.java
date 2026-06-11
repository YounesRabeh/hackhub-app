package com.hackhub.api.controller;

import com.hackhub.api.OpenApiConfig;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
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
@Tag(
	name = "Hackathons",
	description = "Manage hackathons, registrations, submissions, staff assignments, and winner declaration."
)
public class HackathonController {

	private final HackathonService hackathonService;
	private final HackathonRegistrationService hackathonRegistrationService;
	private final SubmissionService submissionService;
	private final OrganizerService organizerService;

	@GetMapping
	@Operation(
		summary = "List hackathons",
		description = "Use case: users browse all available hackathons and their current status."
	)
	public List<HackathonResponse> listHackathons() {
		return hackathonService.listHackathons();
	}

	@GetMapping("/{hackathonId}")
	@Operation(
		summary = "Get hackathon details",
		description = "Use case: a user opens one hackathon page and needs full details by id."
	)
	public HackathonResponse getHackathon(
		@Parameter(description = "Hackathon ID", example = "1")
		@PathVariable("hackathonId") Long hackathonId
	) {
		return hackathonService.getHackathon(hackathonId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
	@Operation(
		summary = "Create hackathon",
		description = "Use case: an organizer creates a new hackathon event with dates and rules."
	)
	public HackathonResponse createHackathon(@Valid @RequestBody CreateHackathonRequest request) {
		return hackathonService.createHackathon(request);
	}

	@PostMapping("/{hackathonId}/mentors/{mentorId}")
	@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
	@Operation(
		summary = "Assign mentor to hackathon",
		description = "Use case: an organizer assigns a mentor so they can support participating teams."
	)
	public HackathonResponse addMentor(
		@Parameter(description = "Hackathon ID", example = "1")
		@PathVariable("hackathonId") Long hackathonId,
		@Parameter(description = "Mentor user ID", example = "3")
		@PathVariable("mentorId") Long mentorId
	) {
		return hackathonService.addMentor(hackathonId, mentorId);
	}

	@PostMapping("/{hackathonId}/judges/{judgeId}")
	@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
	@Operation(
		summary = "Assign judge to hackathon",
		description = "Use case: an organizer assigns a judge who can score submissions during evaluation."
	)
	public HackathonResponse addJudge(
		@Parameter(description = "Hackathon ID", example = "1")
		@PathVariable("hackathonId") Long hackathonId,
		@Parameter(description = "Judge user ID", example = "2")
		@PathVariable("judgeId") Long judgeId
	) {
		return hackathonService.addJudge(hackathonId, judgeId);
	}

	@PatchMapping("/{hackathonId}/status")
	@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
	@Operation(
		summary = "Update hackathon status",
		description = "Use case: staff transitions the hackathon lifecycle (registration, in-progress, evaluation, finished)."
	)
	public HackathonResponse updateStatus(
		@Parameter(description = "Hackathon ID", example = "1")
		@PathVariable("hackathonId") Long hackathonId,
		@Valid @RequestBody UpdateHackathonStatusRequest request
	) {
		return hackathonService.updateStatus(hackathonId, request);
	}

	@PostMapping("/{hackathonId}/registrations")
	@ResponseStatus(HttpStatus.CREATED)
	@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
	@Operation(
		summary = "Register team to hackathon",
		description = "Use case: a team signs up to participate in a specific hackathon."
	)
	public HackathonRegistrationResponse registerTeam(
		@Parameter(description = "Hackathon ID", example = "1")
		@PathVariable("hackathonId") Long hackathonId,
		@Valid @RequestBody RegisterTeamToHackathonRequest request
	) {
		return hackathonRegistrationService.registerTeam(hackathonId, request);
	}

	@GetMapping("/{hackathonId}/registrations")
	@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
	@Operation(
		summary = "List hackathon registrations",
		description = "Use case: organizers review all teams registered for a hackathon."
	)
	public List<HackathonRegistrationResponse> listRegistrations(
		@Parameter(description = "Hackathon ID", example = "1")
		@PathVariable("hackathonId") Long hackathonId
	) {
		return hackathonRegistrationService.listRegistrations(hackathonId);
	}

	@PutMapping("/{hackathonId}/submissions/my-team")
	@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
	@Operation(
		summary = "Create or update my team submission",
		description = "Use case: a team submits or edits their project deliverables for a hackathon."
	)
	public SubmissionResponse upsertMyTeamSubmission(
		@Parameter(description = "Hackathon ID", example = "1")
		@PathVariable("hackathonId") @NonNull Long hackathonId,
		@Valid @RequestBody @NonNull UpsertSubmissionRequest request
	) {
		return submissionService.upsertMyTeamSubmission(hackathonId, request);
	}

	@GetMapping("/{hackathonId}/submissions/my-team")
	@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
	@Operation(
		summary = "Get my team submission",
		description = "Use case: a team views its current submission for a hackathon."
	)
	public SubmissionResponse getMyTeamSubmission(
		@Parameter(description = "Hackathon ID", example = "1")
		@PathVariable("hackathonId") @NonNull Long hackathonId
	) {
		return submissionService.getMyTeamSubmission(hackathonId);
	}

	@GetMapping("/{hackathonId}/submissions")
	@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
	@Operation(
		summary = "List hackathon submissions",
		description = "Use case: assigned mentors or judges review all submissions in a hackathon."
	)
	public List<SubmissionResponse> listSubmissions(
		@Parameter(description = "Hackathon ID", example = "1")
		@PathVariable("hackathonId") @NonNull Long hackathonId
	) {
		return submissionService.listHackathonSubmissions(hackathonId);
	}

	@PostMapping("/{hackathonId}/winner")
	@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
	@Operation(
		summary = "Declare hackathon winner",
		description = "Use case: an organizer sets the winning team after evaluation is completed."
	)
	public HackathonResponse declareWinner(
		@Parameter(description = "Hackathon ID", example = "1")
		@PathVariable("hackathonId") Long hackathonId,
		@Valid @RequestBody DeclareWinnerRequest request
	) {
		return organizerService.declareWinner(hackathonId, request);
	}
}
