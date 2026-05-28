package com.hackhub.application.service;

import com.hackhub.domain.enums.PaymentStatus;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.PaymentTransaction;
import com.hackhub.domain.model.Team;
import com.hackhub.infrastructure.external.payment.PaymentClient;
import com.hackhub.infrastructure.external.payment.PaymentRequest;
import com.hackhub.infrastructure.external.payment.PaymentResponse;
import com.hackhub.infrastructure.repository.PaymentTransactionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentPrizeService {

	private final PaymentClient paymentClient;
	private final PaymentTransactionRepository paymentTransactionRepository;

	@Transactional
	public PaymentTransaction payWinnerPrize(Hackathon hackathon, Team winnerTeam) {
		PaymentRequest request = new PaymentRequest(
			hackathon.getTitle(),
			winnerTeam.getId(),
			hackathon.getPrizeAmount()
		);
		PaymentResponse response = paymentClient.payPrize(request);

		PaymentTransaction transaction = new PaymentTransaction();
		transaction.setHackathon(hackathon);
		transaction.setWinnerTeam(winnerTeam);
		transaction.setAmount(hackathon.getPrizeAmount());
		transaction.setExternalPaymentId(response.externalPaymentId());
		transaction.setStatus(response.successful() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
		transaction.setCreatedAt(LocalDateTime.now());
		if (response.successful()) {
			transaction.setCompletedAt(LocalDateTime.now());
		}

		return paymentTransactionRepository.save(transaction);
	}
}
