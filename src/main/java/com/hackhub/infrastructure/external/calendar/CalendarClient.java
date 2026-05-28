package com.hackhub.infrastructure.external.calendar;

public interface CalendarClient {

	CalendarBookingResponse bookCall(CalendarBookingRequest request);
}
