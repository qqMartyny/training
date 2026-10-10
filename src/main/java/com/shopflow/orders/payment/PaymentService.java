package com.shopflow.orders.payment;

import java.math.BigDecimal;
import java.util.UUID;

import com.shopflow.orders.common.exception.NotFoundException;
import com.shopflow.orders.order.OrderService;
import com.shopflow.orders.order.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

	private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

	private final PaymentProcessor paymentProcessor;

	private final PaymentRepository paymentRepository;

	private final OrderService orderService;

	public PaymentService(PaymentProcessor paymentProcessor, PaymentRepository paymentRepository,
			OrderService orderService) {
		this.paymentProcessor = paymentProcessor;
		this.paymentRepository = paymentRepository;
		this.orderService = orderService;
	}

	public PaymentResult pay(UUID orderId, Long customerId, PaymentMethod method, BigDecimal amount,
			String paymentToken) {
		PaymentRequest request = new PaymentRequest(orderId, customerId, amount, paymentToken);
		PaymentResult result;
		try {
			result = paymentProcessor.process(request);
		}
		catch (RuntimeException ex) {
			log.warn("Payment provider error for order {}: {}", orderId, ex.getMessage());
			result = PaymentResult.failed(null, "PROVIDER_ERROR");
		}
		paymentRepository.save(Payment.from(orderId, method, amount, result));
		log.info("Payment for order {} via {}: {}", orderId, method, result.status());
		return result;
	}

	@Transactional
	public void handleCallback(PaymentCallbackRequest callback) {
		Payment payment = paymentRepository.findByExternalId(callback.externalId())
			.orElseThrow(() -> NotFoundException.of("Payment", callback.externalId()));
		payment.complete(callback.status(), callback.failureReason());
		OrderStatus target = callback.status() == PaymentStatus.SUCCEEDED ? OrderStatus.PAID
				: OrderStatus.PAYMENT_FAILED;
		orderService.changeStatus(payment.getOrderId(), target);
	}

}
