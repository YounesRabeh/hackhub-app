package com.hackhub.api.dto.response;

import com.hackhub.domain.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentTransactionResponse(
	Long id,
	Long hackathonId,
	Long winnerTeamId,
	BigDecimal amount,
	String externalPaymentId,
	PaymentStatus status,
	LocalDateTime createdAt,
	LocalDateTime completedAt
) {
}
