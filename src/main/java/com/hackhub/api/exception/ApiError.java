package com.hackhub.api.exception;

import java.time.Instant;
import java.util.List;

/**
 * Represents a standardized error response for the HackHub REST API.
 * @param timestamp the time the error occurred
 * @param status the HTTP status code
 * @param error a brief description of the error
 * @param message a detailed error message
 * @param path the request path that caused the error
 * @param details additional details about the error, such as validation errors
 */
public record ApiError(
	Instant timestamp,
	int status,
	String error,
	String message,
	String path,
	List<String> details
) {
}
