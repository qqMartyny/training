package com.shopflow.orders.payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.shopflow.orders.order.OrderService;
import com.shopflow.orders.order.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

	private final PaymentRepository paymentRepository = mock(PaymentRepository.class);

	private final OrderService orderService = mock(OrderService.class);

	private final PaymentProcessor sbp = mock(PaymentProcessor.class);

	private PaymentService paymentService() {
		when(sbp.method()).thenReturn(PaymentMethod.SBP);
		return new PaymentService(new PaymentProcessorRegistry(List.of(sbp)), paymentRepository, orderService);
	}

	@Test
	void marksOrderPaidOnSuccessfulCallback() {
		PaymentService service = paymentService();
		Payment payment = pendingPayment();
		when(paymentRepository.findByExternalId("sbp_1")).thenReturn(Optional.of(payment));

		service.handleCallback(new PaymentCallbackRequest("sbp_1", PaymentStatus.SUCCEEDED, null));

		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
		verify(orderService).changeStatus(payment.getOrderId(), OrderStatus.PAID);
	}

	@Test
	void ignoresDuplicateCallback() {
		PaymentService service = paymentService();
		Payment payment = pendingPayment();
		payment.complete(PaymentStatus.SUCCEEDED, null);
		when(paymentRepository.findByExternalId("sbp_1")).thenReturn(Optional.of(payment));

		service.handleCallback(new PaymentCallbackRequest("sbp_1", PaymentStatus.SUCCEEDED, null));

		verify(orderService, never()).changeStatus(any(), any());
	}

	@Test
	void treatsProviderExceptionAsFailedPayment() {
		PaymentService service = paymentService();
		when(sbp.process(any())).thenThrow(new IllegalStateException("timeout"));

		PaymentResult result = service.pay(UUID.randomUUID(), 1L, PaymentMethod.SBP, new BigDecimal("100.00"), null);

		assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
		verify(paymentRepository).save(any(Payment.class));
	}

	private static Payment pendingPayment() {
		return Payment.from(UUID.randomUUID(), PaymentMethod.SBP, new BigDecimal("100.00"),
				PaymentResult.pendingSbp("sbp_1", "qr"));
	}

}
