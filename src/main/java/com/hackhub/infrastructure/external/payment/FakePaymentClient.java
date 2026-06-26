package com.hackhub.infrastructure.external.payment;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FakePaymentClient implements PaymentClient {

	@Override
	public PaymentResponse payPrize(PaymentRequest request) {
		String externalPaymentId = "fake-pay-" + deterministicUuid(
			"payment",
			request.hackathonTitle(),
			request.winnerTeamId(),
			request.amount()
		);
		return new PaymentResponse(externalPaymentId, true);
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
