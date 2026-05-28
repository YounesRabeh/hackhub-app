package com.hackhub.infrastructure.external.payment;

import java.math.BigDecimal;

public record PaymentRequest(
	String hackathonTitle,
	Long winnerTeamId,
	BigDecimal amount
) {
}
