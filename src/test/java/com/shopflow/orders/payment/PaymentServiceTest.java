package com.shopflow.orders.payment;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.shopflow.orders.order.OrderService;
import com.shopflow.orders.order.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

	private final PaymentRepository paymentRepository = mock(PaymentRepository.class);

	private final OrderService orderService = mock(OrderService.class);

	private final PaymentProcessor processor = mock(PaymentProcessor.class);

	private final PaymentService paymentService = new PaymentService(processor, paymentRepository, orderService);

	@Test
	void marksOrderPaidOnSuccessfulCallback() {
		Payment payment = Payment.from(UUID.randomUUID(), PaymentMethod.SBP, new BigDecimal("100.00"),
				PaymentResult.pendingSbp("sbp_1", "qr"));
		when(paymentRepository.findByExternalId("sbp_1")).thenReturn(Optional.of(payment));

		paymentService.handleCallback(new PaymentCallbackRequest("sbp_1", PaymentStatus.SUCCEEDED, null));

		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
		verify(orderService).changeStatus(payment.getOrderId(), OrderStatus.PAID);
	}

	@Test
	void treatsProviderExceptionAsFailedPayment() {
		when(processor.process(any())).thenThrow(new IllegalStateException("timeout"));

		PaymentResult result = paymentService.pay(UUID.randomUUID(), 1L, PaymentMethod.CARD, new BigDecimal("100.00"),
				"tok");

		assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
		verify(paymentRepository).save(any(Payment.class));
	}

}
