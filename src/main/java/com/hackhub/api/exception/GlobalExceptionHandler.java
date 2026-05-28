package com.hackhub.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request, List.of());
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(
		MethodArgumentNotValidException ex,
		HttpServletRequest request
	) {
		List<String> details = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(this::formatFieldError)
			.collect(Collectors.toList());

		return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ApiError> handleBind(BindException ex, HttpServletRequest request) {
		List<String> details = ex.getFieldErrors()
			.stream()
			.map(this::formatFieldError)
			.collect(Collectors.toList());

		return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiError> handleAuthentication(
		AuthenticationException ex,
		HttpServletRequest request
	) {
		return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication failed", request, List.of());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> handleAccessDenied(
		AccessDeniedException ex,
		HttpServletRequest request
	) {
		return buildResponse(HttpStatus.FORBIDDEN, "Access denied", request, List.of());
	}

	@ExceptionHandler({ NoResourceFoundException.class, NoHandlerFoundException.class })
	public ResponseEntity<ApiError> handleMissingRoute(
		Exception ex,
		HttpServletRequest request
	) {
		return buildResponse(HttpStatus.NOT_FOUND, "Resource not found", request, List.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
		return buildResponse(
			HttpStatus.INTERNAL_SERVER_ERROR,
			"Unexpected internal error",
			request,
			List.of()
		);
	}

	private ResponseEntity<ApiError> buildResponse(
		HttpStatus status,
		String message,
		HttpServletRequest request,
		List<String> details
	) {
		ApiError body = new ApiError(
			Instant.now(),
			status.value(),
			status.getReasonPhrase(),
			message,
			request.getRequestURI(),
			details
		);
		return ResponseEntity.status(status).body(body);
	}

	private String formatFieldError(FieldError error) {
		return error.getField() + ": " + (error.getDefaultMessage() == null ? "invalid value" : error.getDefaultMessage());
	}
}
