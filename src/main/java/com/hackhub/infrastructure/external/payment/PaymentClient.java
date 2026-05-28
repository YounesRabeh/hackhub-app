package com.hackhub.infrastructure.external.payment;

public interface PaymentClient {

	PaymentResponse payPrize(PaymentRequest request);
}
