package com.hackhub.infrastructure.external.calendar;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FakeCalendarClient implements CalendarClient {

	@Override
	public CalendarBookingResponse bookCall(CalendarBookingRequest request) {
		String externalCallId = "fake-call-" + deterministicUuid(
			"calendar",
			request.topic(),
			request.scheduledAt(),
			request.requesterEmail(),
			request.mentorEmail()
		);
		String bookingUrl = "https://calendar.fake.local/bookings/" + externalCallId;
		return new CalendarBookingResponse(externalCallId, bookingUrl);
	}

	private UUID deterministicUuid(String type, Object... values) {
		String seed = type + ":" + java.util.Arrays
			.stream(values)
			.map(Objects::toString)
			.reduce((left, right) -> left + ":" + right)
			.orElse("");
		return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
	}
}
