package com.hackhub.infrastructure.external.payment;

public record PaymentResponse(
	String externalPaymentId,
	boolean successful
) {
}
