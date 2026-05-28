package com.hackhub.infrastructure.external.payment;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FakePaymentClient implements PaymentClient {

	@Override
	public PaymentResponse payPrize(PaymentRequest request) {
		String externalPaymentId = "fake-pay-" + UUID.randomUUID();
		return new PaymentResponse(externalPaymentId, true);
	}
}
