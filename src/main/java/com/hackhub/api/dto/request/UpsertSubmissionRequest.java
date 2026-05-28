package com.hackhub.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpsertSubmissionRequest(
	@NotBlank @Size(max = 255) String projectName,
	@NotBlank @Pattern(regexp = "https?://.*", message = "repositoryUrl must be a valid http(s) URL") String repositoryUrl,
	@Pattern(regexp = "https?://.*", message = "demoUrl must be a valid http(s) URL") String demoUrl,
	@NotBlank @Size(max = 4000) String description
) {
}
