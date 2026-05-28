package com.hackhub.infrastructure.external.calendar;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FakeCalendarClient implements CalendarClient {

	@Override
	public CalendarBookingResponse bookCall(CalendarBookingRequest request) {
		String externalCallId = "fake-call-" + UUID.randomUUID();
		String bookingUrl = "https://calendar.fake.local/bookings/" + externalCallId;
		return new CalendarBookingResponse(externalCallId, bookingUrl);
	}
}
