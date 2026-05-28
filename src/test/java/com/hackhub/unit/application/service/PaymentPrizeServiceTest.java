package com.hackhub.unit.application.service;

import com.hackhub.application.service.PaymentPrizeService;
import com.hackhub.domain.enums.HackathonStatus;
import com.hackhub.domain.enums.PaymentStatus;
import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.PaymentTransaction;
import com.hackhub.domain.model.Team;
import com.hackhub.infrastructure.external.payment.PaymentClient;
import com.hackhub.infrastructure.external.payment.PaymentRequest;
import com.hackhub.infrastructure.external.payment.PaymentResponse;
import com.hackhub.infrastructure.repository.PaymentTransactionRepository;
import com.hackhub.testsupport.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentPrizeServiceTest {

	@Mock
	private PaymentClient paymentClient;

	@Mock
	private PaymentTransactionRepository paymentTransactionRepository;

	private PaymentPrizeService paymentPrizeService;

	@BeforeEach
	void setUp() {
		paymentPrizeService = new PaymentPrizeService(paymentClient, paymentTransactionRepository);
	}

	@Test
	void payWinnerPrizeCreatesCompletedTransactionFromSuccessfulClientResponse() {
		Hackathon hackathon = TestDataFactory.hackathon(
			10L,
			TestDataFactory.user(1L, Role.ORGANIZER),
			HackathonStatus.FINISHED
		);
		Team winnerTeam = TestDataFactory.team(20L, TestDataFactory.user(2L, Role.USER), TestDataFactory.user(2L, Role.USER));
		when(paymentClient.payPrize(any(PaymentRequest.class))).thenReturn(new PaymentResponse("pay-123", true));
		when(paymentTransactionRepository.save(any(PaymentTransaction.class))).thenAnswer(invocation -> {
			PaymentTransaction transaction = invocation.getArgument(0);
			transaction.setId(30L);
			return transaction;
		});

		PaymentTransaction transaction = paymentPrizeService.payWinnerPrize(hackathon, winnerTeam);

		ArgumentCaptor<PaymentRequest> requestCaptor = ArgumentCaptor.forClass(PaymentRequest.class);
		verify(paymentClient).payPrize(requestCaptor.capture());
		assertThat(requestCaptor.getValue().amount()).isEqualByComparingTo(hackathon.getPrizeAmount());
		assertThat(requestCaptor.getValue().winnerTeamId()).isEqualTo(20L);
		assertThat(transaction.getExternalPaymentId()).isEqualTo("pay-123");
		assertThat(transaction.getAmount()).isEqualByComparingTo(hackathon.getPrizeAmount());
		assertThat(transaction.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
		assertThat(transaction.getCompletedAt()).isNotNull();
	}

	@Test
	void payWinnerPrizeStoresFailedStatusWhenClientFails() {
		Hackathon hackathon = TestDataFactory.hackathon(
			10L,
			TestDataFactory.user(1L, Role.ORGANIZER),
			HackathonStatus.FINISHED
		);
		Team winnerTeam = TestDataFactory.team(20L, TestDataFactory.user(2L, Role.USER), TestDataFactory.user(2L, Role.USER));
		when(paymentClient.payPrize(any(PaymentRequest.class))).thenReturn(new PaymentResponse("pay-failed", false));
		when(paymentTransactionRepository.save(any(PaymentTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

		PaymentTransaction transaction = paymentPrizeService.payWinnerPrize(hackathon, winnerTeam);

		assertThat(transaction.getExternalPaymentId()).isEqualTo("pay-failed");
		assertThat(transaction.getStatus()).isEqualTo(PaymentStatus.FAILED);
		assertThat(transaction.getCompletedAt()).isNull();
	}
}
