package com.hackhub.infrastructure.external.calendar;

import java.time.LocalDateTime;

public record CalendarBookingRequest(
	String topic,
	LocalDateTime scheduledAt,
	String requesterEmail,
	String mentorEmail
) {
}
