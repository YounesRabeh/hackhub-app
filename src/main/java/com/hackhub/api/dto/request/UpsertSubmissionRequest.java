package com.hackhub.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpsertSubmissionRequest(
	@Schema(example = "AI Study Buddy") @NotBlank @Size(max = 255) String projectName,
	@Schema(example = "https://github.com/hackhub/team-a-project") @NotBlank @Pattern(regexp = "https?://.*", message = "repositoryUrl must be a valid http(s) URL") String repositoryUrl,
	@Schema(example = "https://demo.hackhub.app") @Pattern(regexp = "https?://.*", message = "demoUrl must be a valid http(s) URL") String demoUrl,
	@Schema(example = "An AI assistant that helps students create revision plans.") @NotBlank @Size(max = 4000) String description
) {
}
